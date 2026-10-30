package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.RefreshToken;
import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    public enum Status { OK, GRACE, INVALID }
    public record Rotation(Status status, User user, String newToken) {}

    private static final LocalDateTime ANCIENT = LocalDateTime.of(2000, 1, 1, 0, 0);   // "revocado sin gracia"
    private static final Rotation INVALID = new Rotation(Status.INVALID, null, null);

    private final RefreshTokenRepository repo;
    @Value("${app.auth.refresh-days:7}") private int days;
    @Value("${app.auth.refresh-grace-seconds:10}") private long graceSeconds;
    private final SecureRandom random = new SecureRandom();

    public long ttlSeconds() { return Duration.ofDays(days).toSeconds(); }

    @Transactional
    public String start(User user) { return create(user, UUID.randomUUID().toString()); }

    @Transactional
    public Rotation rotate(String raw) {
        if (raw == null || raw.isBlank()) return INVALID;
        LocalDateTime now = LocalDateTime.now();

        Optional<RefreshToken> found = repo.findByHash(hash(raw));
        if (found.isEmpty()) return INVALID;
        RefreshToken t = found.get();
        if (t.getExpiresAt().isBefore(now)) return INVALID;
        if (!t.getUser().isEnabled()) { repo.revokeFamily(t.getFamilyId(), ANCIENT); return INVALID; }   // usuario desactivado

        if (repo.revokeOne(t.getId(), now) == 1) {                      // ganamos: primera vez que se usa
            return new Rotation(Status.OK, t.getUser(), create(t.getUser(), t.getFamilyId()));
        }
        // Ya estaba usado: ¿carrera legítima entre pestañas, o robo?
        LocalDateTime revoked = repo.revokedAt(t.getId());
        if (revoked != null && revoked.isAfter(now.minusSeconds(graceSeconds)))
            return new Rotation(Status.GRACE, t.getUser(), null);
        repo.revokeFamily(t.getFamilyId(), ANCIENT);                    // reutilización: se invalida toda la cadena
        return INVALID;
    }

    @Transactional
    public void revoke(String raw) {
        if (raw == null || raw.isBlank()) return;
        repo.findByHash(hash(raw)).ifPresent(t -> repo.revokeFamily(t.getFamilyId(), ANCIENT));
    }

    /** Cerrar todas las sesiones de un usuario (al desactivarlo, por ejemplo). */
    @Transactional
    public void revokeAll(Long userId) { repo.revokeAllOfUser(userId, ANCIENT); }

    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void purgeExpired() { repo.purge(LocalDateTime.now().minusDays(1)); }

    private String create(User u, String family) {
        byte[] b = new byte[32];
        random.nextBytes(b);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        RefreshToken t = new RefreshToken();
        t.setTokenHash(hash(raw)); t.setFamilyId(family); t.setUser(u);
        t.setCreatedAt(LocalDateTime.now()); t.setExpiresAt(LocalDateTime.now().plusDays(days));
        repo.save(t);
        return raw;
    }

    private static String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
