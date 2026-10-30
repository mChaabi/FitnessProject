package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.AuthResponse;
import com.example.fitnessBackEnd.dto.LoginRequest;
import com.example.fitnessBackEnd.dto.RegisterRequest;
import com.example.fitnessBackEnd.dto.UserDTO;
import com.example.fitnessBackEnd.exception.ApiError;
import com.example.fitnessBackEnd.exception.UnauthorizedException;
import com.example.fitnessBackEnd.service.AuthService;
import com.example.fitnessBackEnd.service.RefreshTokenService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE = "fitforge_rt";

    private final AuthService auth;
    private final RefreshTokenService refreshTokens;
    @Value("${app.auth.cookie-secure:true}") private boolean secure;
    @Value("${app.auth.cookie-path:/api/auth}") private String cookiePath;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest r, HttpServletRequest req) {
        return ResponseEntity.ok(auth.register(r, req.getRemoteAddr()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r, HttpServletRequest req) {
        return respond(auth.login(r, req.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = COOKIE, required = false) String raw,
                                     @RequestHeader(name = "X-Requested-With", required = false) String xrw) {
        requireAjax(xrw);
        try {
            return respond(auth.refresh(raw));
        } catch (UnauthorizedException e) {                // sesión inválida: se borra también la cookie
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                    .body(new ApiError(401, e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String raw,
                                       @RequestHeader(name = "X-Requested-With", required = false) String xrw) {
        requireAjax(xrw);
        auth.logout(raw);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString()).build();
    }

    private ResponseEntity<AuthResponse> respond(AuthService.AuthResult r) {
        ResponseEntity.BodyBuilder b = ResponseEntity.ok().cacheControl(CacheControl.noStore());   // nunca cachear tokens
        if (r.refreshToken() != null)
            b.header(HttpHeaders.SET_COOKIE, cookie(r.refreshToken(), Duration.ofSeconds(refreshTokens.ttlSeconds())).toString());
        return b.body(r.body());
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE, value).httpOnly(true).secure(secure)
                .sameSite("Strict").path(cookiePath).maxAge(maxAge).build();
    }

    private void requireAjax(String header) {
        if (!"fitforge".equals(header)) throw new AccessDeniedException("Missing header");
    }
}