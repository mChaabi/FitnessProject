package com.example.fitnessBackEnd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.OptionalLong;

@Component
public class UnsubscribeTokens {

    private final byte[] key;

    public UnsubscribeTokens(@Value("${app.mail.unsub-secret:}") String secret) {
        if (secret == null || secret.length() < 32)
            throw new IllegalStateException("UNSUB_SECRET debe tener al menos 32 caracteres");
        this.key = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String create(long memberId) {
        String id = Long.toString(memberId);
        return id + "." + sign(id);
    }

    public OptionalLong verify(String token) {
        if (token == null) return OptionalLong.empty();
        int dot = token.indexOf('.');
        if (dot < 1) return OptionalLong.empty();
        String id = token.substring(0, dot), sig = token.substring(dot + 1);
        if (!MessageDigest.isEqual(sign(id).getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8)))
            return OptionalLong.empty();
        try { return OptionalLong.of(Long.parseLong(id)); } catch (NumberFormatException e) { return OptionalLong.empty(); }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
}
