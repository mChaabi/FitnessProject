package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p join fetch p.subscription s join fetch s.member join fetch s.plan where p.id = :id")
    Optional<Payment> findDetailed(@Param("id") Long id);

    @Query("select p from Payment p join fetch p.subscription s join fetch s.plan " +
            "where s.member.id = :memberId order by s.startDate desc")
    List<Payment> findHistory(@Param("memberId") Long memberId);

    @Query(value = "select p from Payment p join fetch p.subscription s join fetch s.member join fetch s.plan",
            countQuery = "select count(p) from Payment p")
    Page<Payment> findPage(Pageable pageable);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paidAt >= :from and p.paidAt < :to")
    BigDecimal sumBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}