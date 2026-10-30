package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Booking;
import com.example.fitnessBackEnd.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findBySessionIdAndMemberId(Long sessionId, Long memberId);
    long countBySessionIdAndStatus(Long sessionId, BookingStatus status);
    long countBySessionIdAndStatusAndQueuedAtBefore(Long sessionId, BookingStatus status, LocalDateTime t);
    List<Booking> findBySessionIdAndStatusOrderByQueuedAtAsc(Long sessionId, BookingStatus status);

    // contadores de toda la semana en UNA consulta (evita una por sesión)
    @Query("select b.session.id, b.status, count(b) from Booking b " +
            "where b.session.sessionDate between :from and :to and b.status <> :cancelled " +
            "group by b.session.id, b.status")
    List<Object[]> counts(@Param("from") LocalDate from, @Param("to") LocalDate to,
                          @Param("cancelled") BookingStatus cancelled);

    @Query("select b from Booking b where b.member.id = :m and b.status <> :cancelled " +
            "and b.session.sessionDate between :from and :to")
    List<Booking> mineInRange(@Param("m") Long m, @Param("from") LocalDate from, @Param("to") LocalDate to,
                              @Param("cancelled") BookingStatus cancelled);

    @Query("select b from Booking b join fetch b.member where b.session.id = :id and b.status <> :cancelled " +
            "order by b.status, b.queuedAt")
    List<Booking> attendees(@Param("id") Long id, @Param("cancelled") BookingStatus cancelled);

    @Query("select b from Booking b join fetch b.session join fetch b.member m left join fetch m.user where b.id = :id")
    Optional<Booking> findDetailed(@Param("id") Long id);

    @Query("select count(b) from Booking b where b.member.id = :m and b.status <> :cancelled " +
            "and b.session.sessionDate = :d and b.session.startTime < :end and b.session.endTime > :start " +
            "and b.session.id <> :sid")
    long countOverlap(@Param("m") Long m, @Param("d") LocalDate d, @Param("start") LocalTime start,
                      @Param("end") LocalTime end, @Param("sid") Long sid, @Param("cancelled") BookingStatus cancelled);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Booking b set b.status = :cancelled where b.session.id = :id and b.status <> :cancelled")
    int cancelAll(@Param("id") Long id, @Param("cancelled") BookingStatus cancelled);
}
