package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.CheckIn;
import com.example.fitnessBackEnd.enums.CheckInOutcome;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    boolean existsByMemberIdAndOutcomeAndCheckedAtAfter(Long memberId, CheckInOutcome o, LocalDateTime after);

    @Query("select c from CheckIn c join fetch c.member " +
            "where c.checkedAt >= :from and c.checkedAt < :to order by c.checkedAt desc")
    List<CheckIn> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable page);

    @Query("select count(distinct c.member.id) from CheckIn c " +
            "where c.outcome = :o and c.checkedAt >= :from and c.checkedAt < :to")
    long countMembers(@Param("o") CheckInOutcome o, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select c.member.id, max(c.checkedAt) from CheckIn c " +
            "where c.member.id in :ids and c.outcome = :ok group by c.member.id")
    List<Object[]> lastVisits(@Param("ids") Collection<Long> ids, @Param("ok") CheckInOutcome ok);

    @Query("select c.checkedAt from CheckIn c where c.outcome = :ok and c.checkedAt >= :from")
    List<LocalDateTime> visitTimes(@Param("from") LocalDateTime from, @Param("ok") CheckInOutcome ok);
}
