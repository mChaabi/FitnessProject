package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.enums.SessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<ClassSession, Long> {

    boolean existsByTemplateIdAndSessionDate(Long templateId, LocalDate date);

    @Query("select s from ClassSession s join fetch s.coach c join fetch c.user " +
            "where s.sessionDate between :from and :to order by s.sessionDate, s.startTime")
    List<ClassSession> findInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select s from ClassSession s join fetch s.coach c join fetch c.user where s.id = :id")
    Optional<ClassSession> findDetailed(@Param("id") Long id);

    /** Bloquea la fila: nadie más reserva en esta sesión hasta que termine la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ClassSession s where s.id = :id")
    Optional<ClassSession> lockById(@Param("id") Long id);

    @Query("select count(s) from ClassSession s where s.coach.id = :coach and s.sessionDate = :d " +
            "and s.status = :st and s.startTime < :end and s.endTime > :start")
    long countCoachOverlap(@Param("coach") Long coach, @Param("d") LocalDate d, @Param("start") LocalTime start,
                           @Param("end") LocalTime end, @Param("st") SessionStatus st);
}
