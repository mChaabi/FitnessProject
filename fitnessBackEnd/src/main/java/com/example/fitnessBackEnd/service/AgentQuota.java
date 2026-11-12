package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.AgentRun;
import com.example.fitnessBackEnd.exception.TooManyRequestsException;
import com.example.fitnessBackEnd.repository.AgentRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgentQuota {

    private final AgentRunRepository runs;

    /** Throws 429 past the daily limit, otherwise records a RUNNING run. memberId 0 = no member. */
    public AgentRun start(String user, String graph, Long memberId, int maxPerDay) {
        if (runs.countByStartedByAndGraphAndCreatedAtAfter(user, graph, LocalDate.now().atStartOfDay()) >= maxPerDay)
            throw new TooManyRequestsException(3600);
        AgentRun r = new AgentRun();
        r.setId(UUID.randomUUID().toString()); r.setGraph(graph); r.setStartedBy(user);
        r.setMemberId(memberId); r.setStatus("RUNNING"); r.setCreatedAt(LocalDateTime.now());
        return runs.save(r);
    }

    public void finish(AgentRun r, String status) {
        r.setStatus(status); r.setAttempts(1); r.setFinishedAt(LocalDateTime.now());
        runs.save(r);
    }
}