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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository repo;
    private final AuditService audit;

    @Transactional(readOnly = true) public List<PlanResponse> activePlans() {
        return repo.findByActiveTrueOrderByPriceAsc().stream().map(this::map).toList(); }

    @Transactional(readOnly = true) public List<PlanResponse> all() {
        return repo.findAll(Sort.by("price")).stream().map(this::map).toList(); }

    @Transactional public PlanResponse create(PlanRequest r) {
        if (repo.existsByNameIgnoreCase(r.name().trim())) throw new ConflictException("Ce plan existe déjà");
        MembershipPlan p = repo.save(MembershipPlan.builder().name(r.name().trim()).price(r.price())
                .durationDays(r.durationDays()).description(r.description()).active(r.active()).build());
        audit.record("PLAN_CREATED", "PLAN", p.getId(), "Offre créée : « " + p.getName() + " » à "
                + p.getPrice().toPlainString() + " MAD pour " + p.getDurationDays() + " jours");
        return map(p);
    }

    @Transactional public PlanResponse update(Long id, PlanRequest r) {
        MembershipPlan p = repo.findById(id).orElseThrow(() -> new NotFoundException("Plan introuvable"));
        if (repo.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) throw new ConflictException("Ce plan existe déjà");
        BigDecimal oldPrice = p.getPrice();
        boolean wasActive = p.isActive();
        p.setName(r.name().trim()); p.setPrice(r.price()); p.setDurationDays(r.durationDays());
        p.setDescription(r.description()); p.setActive(r.active());

        boolean priceChanged = oldPrice.compareTo(r.price()) != 0, toggled = wasActive != r.active();
        if (priceChanged) audit.record("PLAN_PRICE_CHANGED", "PLAN", id, "Prix de « " + p.getName() + " » : "
                + oldPrice.toPlainString() + " → " + r.price().toPlainString() + " MAD");
        if (toggled) audit.record(r.active() ? "PLAN_ACTIVATED" : "PLAN_DEACTIVATED", "PLAN", id,
                "Offre « " + p.getName() + " » " + (r.active() ? "activée" : "désactivée"));
        if (!priceChanged && !toggled) audit.record("PLAN_UPDATED", "PLAN", id, "Offre modifiée : « " + p.getName() + " »");
        return map(p);   // changing the price does not affect subscriptions already sold
    }

    private PlanResponse map(MembershipPlan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getPrice(), p.getDurationDays(), p.getDescription(), p.isActive());
    }
}