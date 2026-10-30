package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<Member> findByQrCode(String qrCode);

    @Query("""
        select m from Member m
        where (:status is null or m.status = :status)
          and (:k = '' or lower(m.firstName) like lower(concat('%', :k, '%'))
                       or lower(m.lastName)  like lower(concat('%', :k, '%'))
                       or lower(m.email)     like lower(concat('%', :k, '%'))
                       or m.phone like concat('%', :k, '%'))
        """)
    Page<Member> search(@Param("status") MemberStatus status,
                        @Param("k") String keyword, Pageable pageable);

    Page<Member> findByCoachId(Long coachId, Pageable p);
    long countByCoachId(Long coachId);
    boolean existsByUserId(Long userId);

    @Query("select m from Member m where lower(m.user.email) = lower(:email)")
    Optional<Member> findByUserEmail(@Param("email") String email);

    @Modifying
    @Query("update Member m set m.coach = null where m.coach.id = :id")
    void clearCoach(@Param("id") Long id);

    long countByStatus(MemberStatus status);
    long countByJoinedAtGreaterThanEqual(LocalDate date);

    // activos que no han entrado (con éxito) desde "since" y llevan inscritos más tiempo que eso
    @Query("""
    select m from Member m
    where m.status = :active and m.joinedAt < :since
      and not exists (select 1 from CheckIn c where c.member = m
                      and c.outcome = :ok and c.checkedAt >= :sinceTime)
    order by m.lastName, m.firstName
    """)
    List<Member> findAtRisk(@Param("active") MemberStatus active, @Param("since") LocalDate since,
                            @Param("sinceTime") LocalDateTime sinceTime, @Param("ok") CheckInOutcome ok,
                            Pageable page);

    @Query("""
    select count(m) from Member m
    where m.status = :active and m.joinedAt < :since
      and not exists (select 1 from CheckIn c where c.member = m
                      and c.outcome = :ok and c.checkedAt >= :sinceTime)
    """)
    long countAtRisk(@Param("active") MemberStatus active, @Param("since") LocalDate since,
                     @Param("sinceTime") LocalDateTime sinceTime, @Param("ok") CheckInOutcome ok);

    @Transactional
    @Modifying
    @Query("update Member m set m.emailOptOut = true where m.id = :id")
    int optOut(@Param("id") Long id);
}