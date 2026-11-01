package com.example.fitnessBackEnd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_runs")
@Getter
@Setter
@NoArgsConstructor
public class AgentRun {
    @Id
    @Column(length = 36) private String id;
    @Column(nullable = false, length = 50) private String graph;
    @Column(name = "started_by", nullable = false, length = 150) private String startedBy;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(nullable = false, length = 20) private String status;       // RUNNING, READY, FAILED
    @Column(nullable = false) private int attempts;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "finished_at") private LocalDateTime finishedAt;
    @Column(length = 500) private String error;
}
