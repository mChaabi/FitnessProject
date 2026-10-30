package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.EmailMessage;
import com.example.fitnessBackEnd.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<EmailMessage, Long> {

    boolean existsByDedupKey(String key);
    Page<EmailMessage> findByStatus(EmailStatus status, Pageable p);
    long countByStatus(EmailStatus status);

    @Query("select m from EmailMessage m where m.status = :st and m.nextAttemptAt <= :now order by m.createdAt")
    List<EmailMessage> due(@Param("st") EmailStatus st, @Param("now") LocalDateTime now, Pageable p);

    @Transactional
    @Modifying
    @Query("update EmailMessage m set m.status = :to, m.attempts = m.attempts + 1, m.claimedAt = :now " +
            "where m.id = :id and m.status = :from")
    int claim(@Param("id") Long id, @Param("from") EmailStatus from, @Param("to") EmailStatus to, @Param("now") LocalDateTime now);

    @Transactional @Modifying
    @Query("update EmailMessage m set m.status = :to where m.status = :from and m.claimedAt < :before")
    int releaseStuck(@Param("from") EmailStatus from, @Param("to") EmailStatus to, @Param("before") LocalDateTime before);

    @Transactional @Modifying
    @Query("update EmailMessage m set m.status = :sent, m.sentAt = :now, m.aiGenerated = :ai, m.lastError = null where m.id = :id")
    void markSent(@Param("id") Long id, @Param("sent") EmailStatus sent, @Param("now") LocalDateTime now, @Param("ai") boolean ai);

    @Transactional @Modifying
    @Query("update EmailMessage m set m.status = :st, m.nextAttemptAt = :next, m.lastError = :err where m.id = :id")
    void markRetry(@Param("id") Long id, @Param("st") EmailStatus st, @Param("next") LocalDateTime next, @Param("err") String err);

    @Transactional @Modifying
    @Query("update EmailMessage m set m.status = :st, m.lastError = :err where m.id = :id")
    void markFinal(@Param("id") Long id, @Param("st") EmailStatus st, @Param("err") String err);

    @Transactional @Modifying
    @Query("update EmailMessage m set m.status = :to, m.attempts = 0, m.nextAttemptAt = :now, m.lastError = null " +
            "where m.id = :id and m.status = :from")
    int requeue(@Param("id") Long id, @Param("from") EmailStatus from, @Param("to") EmailStatus to, @Param("now") LocalDateTime now);
}