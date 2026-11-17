package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.AuditEntry;
import com.example.fitnessBackEnd.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repo;

    /** Joins the caller's transaction: a rolled-back action leaves no trace. */
    public void record(String action, String entityType, Long entityId, String summary) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        boolean human = a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);

        AuditEntry e = new AuditEntry();
        e.setCreatedAt(LocalDateTime.now());
        e.setActor(human ? cut(a.getName(), 150) : "system");        // scheduled jobs and tests have no user
        e.setActorRole(human ? role(a) : null);
        e.setAction(cut(action, 40));
        e.setEntityType(cut(entityType, 20));
        e.setEntityId(entityId);
        e.setSummary(cut(summary, 300));                             // never longer than the column: the log cannot break an action
        repo.save(e);
    }

    private static String role(Authentication a) {
        return a.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(x -> x.startsWith("ROLE_")).map(x -> x.substring(5)).findFirst().orElse(null);
    }

    private static String cut(String s, int max) {
        String t = s == null ? "" : s.replaceAll("\\s+", " ").trim();
        return t.length() > max ? t.substring(0, max - 1) + "…" : t;
    }
}