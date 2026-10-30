package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.entity.EmailMessage;
import com.example.fitnessBackEnd.enums.EmailStatus;
import com.example.fitnessBackEnd.enums.EmailType;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/outbox") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OutboxController {

    public record OutboxRow(Long id, EmailType type, String recipient, EmailStatus status, int attempts,
                            LocalDateTime createdAt, LocalDateTime sentAt, Boolean aiGenerated, String lastError) {}

    private final OutboxRepository repo;

    @GetMapping
    public Page<OutboxRow> list(@RequestParam(required = false) EmailStatus status,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EmailMessage> r = status == null ? repo.findAll(p) : repo.findByStatus(status, p);
        return r.map(m -> new OutboxRow(m.getId(), m.getType(), m.getRecipient(), m.getStatus(), m.getAttempts(),
                m.getCreatedAt(), m.getSentAt(), m.getAiGenerated(), m.getLastError()));
    }

    @GetMapping("/stats")
    public Map<EmailStatus, Long> stats() {
        Map<EmailStatus, Long> out = new EnumMap<>(EmailStatus.class);
        for (EmailStatus s : EmailStatus.values()) out.put(s, repo.countByStatus(s));
        return out;
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retry(@PathVariable Long id) {
        if (repo.requeue(id, EmailStatus.FAILED, EmailStatus.PENDING, LocalDateTime.now()) == 0)
            throw new ConflictException("Seuls les emails en échec peuvent être relancés");
        return ResponseEntity.noContent().build();
    }
}