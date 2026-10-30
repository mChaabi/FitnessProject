package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    @Query("select c from Coach c join fetch c.user order by c.user.nom")
    List<Coach> findAllWithUser();

    @Query("select c from Coach c join fetch c.user where lower(c.user.email) = lower(:email)")
    Optional<Coach> findByUserEmail(@Param("email") String email);
}