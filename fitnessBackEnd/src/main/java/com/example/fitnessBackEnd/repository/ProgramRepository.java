package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<WorkoutProgram, Long> {

    // detalle completo: una sola consulta, sin N+1
    @Query("select distinct p from WorkoutProgram p join fetch p.member join fetch p.coach c join fetch c.user " +
            "left join fetch p.items i left join fetch i.exercice where p.id = :id")
    Optional<WorkoutProgram> findDetailed(@Param("id") Long id);

    // listas resumidas (sin líneas)
    @Query("select p from WorkoutProgram p join fetch p.member join fetch p.coach " +
            "where p.member.id = :memberId order by p.startDate desc")
    List<WorkoutProgram> findByMember(@Param("memberId") Long memberId);

    // lo que ve el miembro: programas activos con todo cargado
    @Query("select distinct p from WorkoutProgram p join fetch p.member join fetch p.coach c join fetch c.user " +
            "left join fetch p.items i left join fetch i.exercice " +
            "where p.member.id = :memberId and p.active = true order by p.startDate desc")
    List<WorkoutProgram> findActiveFull(@Param("memberId") Long memberId);
}
