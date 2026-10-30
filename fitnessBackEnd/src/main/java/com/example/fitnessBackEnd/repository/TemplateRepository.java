package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.ClassTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TemplateRepository extends JpaRepository<ClassTemplate, Long> {
    @Query("select t from ClassTemplate t join fetch t.coach c join fetch c.user order by t.dayOfWeek, t.startTime")
    List<ClassTemplate> findAllWithCoach();

    @Query("select t from ClassTemplate t join fetch t.coach c join fetch c.user where t.active = true")
    List<ClassTemplate> findActiveWithCoach();
}
