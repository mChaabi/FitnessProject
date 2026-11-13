package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.FaceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FaceRepository extends JpaRepository<FaceTemplate, Long> {
    Optional<FaceTemplate> findByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);

    /** Only ACTIVE members can be matched; a deactivated member never opens the door. */
    @Query("select f from FaceTemplate f join fetch f.member m where m.status <> com.example.fitnessBackEnd.enums.MemberStatus.INACTIVE")
    List<FaceTemplate> findMatchable();
}