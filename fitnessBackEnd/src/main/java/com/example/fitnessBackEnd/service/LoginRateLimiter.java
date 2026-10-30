package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LoginRateLimiter {

    private static final class Counter {
        int failures; Instant windowStart; volatile Instant lockedUntil;
    }

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final Duration lock;

    public LoginRateLimiter(@Value("${app.auth.lock-minutes:15}") int lockMinutes) {
        this.lock = Duration.ofMinutes(lockMinutes);
    }

    /** Lanza 429 si la clave está bloqueada. */
    public void check(String key) {
        Counter c = counters.get(key);
        if (c == null) return;
        Instant now = Instant.now(), until = c.lockedUntil;
        if (until != null && until.isAfter(now))
            throw new TooManyRequestsException(Duration.between(now, until).toSeconds() + 1);
    }

    public void failure(String key, int max) {
        Instant now = Instant.now();
        counters.compute(key, (k, c) -> {
            if (c == null || c.windowStart.plus(lock).isBefore(now)) { c = new Counter(); c.windowStart = now; }
            if (++c.failures >= max) c.lockedUntil = now.plus(lock);
            return c;
        });
    }

    public void success(String key) { counters.remove(key); }

    /** Evita que el mapa crezca sin límite (un atacante puede probar millones de emails). */
    @Scheduled(fixedDelay = 600_000)
    void purge() {
        Instant now = Instant.now();
        counters.entrySet().removeIf(e -> e.getValue().windowStart.plus(lock).isBefore(now)
                && (e.getValue().lockedUntil == null || e.getValue().lockedUntil.isBefore(now)));
    }
}
