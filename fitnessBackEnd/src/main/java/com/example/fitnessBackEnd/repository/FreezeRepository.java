package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.MembershipFreeze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FreezeRepository extends JpaRepository<MembershipFreeze, Long> {

    Optional<MembershipFreeze> findFirstByMemberIdAndStatus(Long memberId, String status);

    List<MembershipFreeze> findByMemberIdOrderByStartDateDesc(Long memberId);

    @Query("select f from MembershipFreeze f join fetch f.member where f.status = 'ACTIVE' and f.resumeDate <= :today")
    List<MembershipFreeze> findDue(@Param("today") LocalDate today);
}