package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findByMemberIdOrderByMeasuredOnAsc(Long memberId);
    boolean existsByMemberIdAndMeasuredOn(Long memberId, LocalDate day);

    @Query("select b from BodyMeasurement b join fetch b.member m left join fetch m.coach c " +
            "left join fetch c.user left join fetch m.user where b.id = :id")
    Optional<BodyMeasurement> findDetailed(@Param("id") Long id);
}
