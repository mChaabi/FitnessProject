package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.PlanRequest;
import com.example.fitnessBackEnd.dto.PlanResponse;
import com.example.fitnessBackEnd.entity.MembershipPlan;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository repo;

    @Transactional(readOnly = true) public List<PlanResponse> activePlans() {
        return repo.findByActiveTrueOrderByPriceAsc().stream().map(this::map).toList(); }

    @Transactional(readOnly = true) public List<PlanResponse> all() {
        return repo.findAll(Sort.by("price")).stream().map(this::map).toList(); }

    @Transactional public PlanResponse create(PlanRequest r) {
        if (repo.existsByNameIgnoreCase(r.name().trim())) throw new ConflictException("Ce plan existe déjà");
        return map(repo.save(MembershipPlan.builder().name(r.name().trim()).price(r.price())
                .durationDays(r.durationDays()).description(r.description()).active(r.active()).build()));
    }

    @Transactional public PlanResponse update(Long id, PlanRequest r) {
        MembershipPlan p = repo.findById(id).orElseThrow(() -> new NotFoundException("Plan introuvable"));
        if (repo.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) throw new ConflictException("Ce plan existe déjà");
        p.setName(r.name().trim()); p.setPrice(r.price()); p.setDurationDays(r.durationDays());
        p.setDescription(r.description()); p.setActive(r.active());
        return map(p);   // cambiar el precio no afecta a suscripciones ya vendidas
    }

    private PlanResponse map(MembershipPlan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getPrice(), p.getDurationDays(), p.getDescription(), p.isActive());
    }
}