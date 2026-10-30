package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.AuthResponse;
import com.example.fitnessBackEnd.dto.LoginRequest;
import com.example.fitnessBackEnd.dto.RegisterRequest;
import com.example.fitnessBackEnd.dto.UserDTO;
import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.UnauthorizedException;
import com.example.fitnessBackEnd.mapper.UserMapper;
import com.example.fitnessBackEnd.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service @RequiredArgsConstructor @Slf4j
public class AuthService {

    public record AuthResult(AuthResponse body, String refreshToken) {}

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter limiter;
    private final RefreshTokenService refreshTokens;

    @Value("${app.auth.max-failures:5}") private int maxFailures;
    private String dummyHash;

    @PostConstruct
    void init() { dummyHash = passwordEncoder.encode("not-a-real-password"); }

    public UserDTO register(RegisterRequest request, String ip) {
        String key = "reg|" + ip;                       // máximo 10 registros por IP cada 15 minutos
        limiter.check(key);
        limiter.failure(key, 10);

        if (userRepository.existsByEmail(request.email().trim().toLowerCase()))
            throw new ConflictException("Email déjà utilisé");
        User user = userMapper.toEntity(request);
        user.setRole(Role.MEMBER);                       // nunca el rol que mande el cliente
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        user.setEnabled(true);
        return userMapper.toDTO(userRepository.save(user));
    }

    public AuthResult login(LoginRequest req, String ip) {
        String email = req.email().trim().toLowerCase();
        String accountKey = ip + "|" + email, ipKey = "ip|" + ip;
        limiter.check(accountKey);
        limiter.check(ipKey);

        Optional<User> found = userRepository.findByEmailIgnoreCase(email).filter(User::isEnabled);
        // BCrypt se ejecuta SIEMPRE: el tiempo de respuesta no revela si el email existe
        boolean ok = passwordEncoder.matches(req.motDePasse(), found.map(User::getMotDePasse).orElse(dummyHash))
                && found.isPresent();

        if (!ok) {
            limiter.failure(accountKey, maxFailures);
            limiter.failure(ipKey, maxFailures * 5);
            log.warn("Login fallido para {} desde {}", mask(email), ip);
            throw new UnauthorizedException("Email ou mot de passe incorrect");   // mismo mensaje en todos los casos
        }

        limiter.success(accountKey);                     // no se resetea el contador por IP: un atacante con cuenta propia lo evitaría
        User u = found.get();
        String access = jwtService.generate(u.getEmail(), u.getRole());
        return new AuthResult(new AuthResponse(access, userMapper.toDTO(u)), refreshTokens.start(u));
    }

    public AuthResult refresh(String rawRefresh) {
        RefreshTokenService.Rotation r = refreshTokens.rotate(rawRefresh);
        if (r.status() == RefreshTokenService.Status.INVALID) throw new UnauthorizedException("Session expirée");
        User u = r.user();
        String access = jwtService.generate(u.getEmail(), u.getRole());
        return new AuthResult(new AuthResponse(access, userMapper.toDTO(u)), r.newToken());   // newToken null en periodo de gracia
    }

    public void logout(String rawRefresh) { refreshTokens.revoke(rawRefresh); }

    private static String mask(String e) {               // nunca se escribe un email completo en los logs
        int at = e.indexOf('@');
        return at < 2 ? "***" : e.charAt(0) + "***" + e.substring(at);
    }
}