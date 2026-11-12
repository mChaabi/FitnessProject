package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.EnrollmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentRequest, Long> {
    Optional<EnrollmentRequest> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, String status);
    List<EnrollmentRequest> findByMemberIdAndStatus(Long memberId, String status);

    @Query("select r from EnrollmentRequest r join fetch r.member left join fetch r.plan " +
            "left join fetch r.coach c left join fetch c.user where r.status = :st order by r.createdAt")
    List<EnrollmentRequest> findByStatusDetailed(@Param("st") String st);
}