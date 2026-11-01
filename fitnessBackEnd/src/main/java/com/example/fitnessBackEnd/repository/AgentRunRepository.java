package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.AgentRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AgentRunRepository extends JpaRepository<AgentRun, String> {
    long countByStartedByAndGraphAndCreatedAtAfter(String user, String graph, LocalDateTime after);
}
