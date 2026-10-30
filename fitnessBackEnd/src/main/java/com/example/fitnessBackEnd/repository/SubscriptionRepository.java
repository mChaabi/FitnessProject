package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByMemberIdAndStatusOrderByEndDateDesc(Long memberId, SubscriptionStatus s);
    boolean existsByMemberIdAndStatus(Long memberId, SubscriptionStatus s);
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus s, LocalDate date);

    @Query("select s from Subscription s where s.member.id = :id and s.status = :status " +
            "and s.startDate <= :day and s.endDate >= :day order by s.endDate desc")
    List<Subscription> findCovering(@Param("id") Long id, @Param("status") SubscriptionStatus status,
                                    @Param("day") LocalDate day);

    boolean existsByMemberId(Long memberId);

    // terminan pronto y NO tienen otra suscripción posterior (renovación ya pagada)
    @Query("""
    select s from Subscription s join fetch s.member join fetch s.plan
    where s.status = :st and s.endDate between :from and :to
      and not exists (select 1 from Subscription n where n.member = s.member
                      and n.status = :st and n.startDate > s.endDate)
    order by s.endDate
    """)
    List<Subscription> findExpiring(@Param("st") SubscriptionStatus st,
                                    @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select s.plan.name, count(s) from Subscription s where s.status = :st " +
            "and s.startDate <= :day and s.endDate >= :day group by s.plan.name order by count(s) desc")
    List<Object[]> planDistribution(@Param("st") SubscriptionStatus st, @Param("day") LocalDate day);
}
