package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.EmailMessage;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.EmailStatus;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDispatcher {

    private static final int BATCH = 10;

    private final OutboxRepository outbox;
    private final MemberRepository members;
    private final EmailRenderer renderer;
    private final AppMailSender sender; // <-- Actualizado aquí
    private final UnsubscribeTokens tokens;

    @Value("${app.frontend-url}") private String frontend;
    @Value("${app.public-api-url}") private String api;
    @Value("${app.mail.max-attempts:5}") private int maxAttempts;
    @Value("${app.mail.send-delay-ms:1200}") private long delayMs;

    @Scheduled(fixedDelayString = "${app.mail.dispatch-interval-ms:15000}")
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        outbox.releaseStuck(EmailStatus.SENDING, EmailStatus.PENDING, now.minusMinutes(10));

        for (EmailMessage m : outbox.due(EmailStatus.PENDING, now, PageRequest.of(0, BATCH))) {
            if (outbox.claim(m.getId(), EmailStatus.PENDING, EmailStatus.SENDING, LocalDateTime.now()) == 0) continue;
            process(m, m.getAttempts() + 1);
            try { Thread.sleep(delayMs); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    private void process(EmailMessage m, int attempt) {
        try {
            boolean optional = m.getType().isRespectsOptOut();
            if (optional && members.findById(m.getMemberId()).map(Member::isEmailOptOut).orElse(true)) {
                outbox.markFinal(m.getId(), EmailStatus.SKIPPED, "Baja del miembro");
                return;
            }
            String token = optional ? tokens.create(m.getMemberId()) : null;
            String pageUrl = token == null ? null : frontend + "/unsubscribe?t=" + token;
            String oneClick = token == null ? null : api + "/notifications/unsubscribe?t=" + token;

            EmailRenderer.Rendered r = renderer.render(m, pageUrl);
            sender.send(m.getRecipient(), r, oneClick);
            outbox.markSent(m.getId(), EmailStatus.SENT, LocalDateTime.now(), r.ai());
            log.info("Email {} enviado (id {})", m.getType(), m.getId());

        } catch (Exception e) {
            String err = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
            err = err.length() > 480 ? err.substring(0, 480) : err;
            if (attempt >= maxAttempts) {
                outbox.markFinal(m.getId(), EmailStatus.FAILED, err);
                log.error("Email {} abandonado tras {} intentos: {}", m.getId(), attempt, err);
            } else {
                long wait = (long) Math.pow(2, attempt);
                outbox.markRetry(m.getId(), EmailStatus.PENDING, LocalDateTime.now().plusMinutes(wait), err);
                log.warn("Email {} falló (intento {}), reintento en {} min: {}", m.getId(), attempt, wait, err);
            }
        }
    }
}