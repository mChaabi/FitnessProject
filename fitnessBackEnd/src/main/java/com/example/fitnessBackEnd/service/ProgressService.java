package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.MeasurementRequest;
import com.example.fitnessBackEnd.dto.MeasurementResponse;
import com.example.fitnessBackEnd.entity.BodyMeasurement;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.MeasurementRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final MeasurementRepository repo;
    private final MemberRepository memberRepo;

    @Transactional(readOnly = true)
    public List<MeasurementResponse> listFor(Long memberId, Authentication a) {
        Member m = member(memberId);
        assertAccess(m, a);
        return repo.findByMemberIdOrderByMeasuredOnAsc(memberId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> listMine(Authentication a) {
        return listFor(mine(a).getId(), a);
    }

    @Transactional
    public MeasurementResponse add(Long memberId, MeasurementRequest r, Authentication a) {
        Member m = member(memberId);
        assertAccess(m, a);

        if (r.weightKg() == null && r.bodyFatPercent() == null && r.waistCm() == null
                && r.chestCm() == null && r.armCm() == null) {
            throw new BadRequestException("Saisissez au moins une mesure");
        }
        if (repo.existsByMemberIdAndMeasuredOn(memberId, r.measuredOn())) {
            throw new ConflictException("Une mesure existe déjà pour cette date");
        }

        BodyMeasurement b = new BodyMeasurement();
        b.setMember(m); b.setMeasuredOn(r.measuredOn());
        b.setWeightKg(r.weightKg()); b.setBodyFatPercent(r.bodyFatPercent());
        b.setWaistCm(r.waistCm()); b.setChestCm(r.chestCm()); b.setArmCm(r.armCm());
        b.setNotes(r.notes()); b.setRecordedBy(a.getName());
        return map(repo.save(b));
    }

    @Transactional
    public MeasurementResponse addMine(MeasurementRequest r, Authentication a) {
        return add(mine(a).getId(), r, a);
    }

    @Transactional
    public void delete(Long id, Authentication a) {
        BodyMeasurement b = repo.findDetailed(id).orElseThrow(() -> new NotFoundException("Mesure introuvable"));
        assertAccess(b.getMember(), a);
        repo.delete(b);
    }

    // ---------- reglas ----------
    private void assertAccess(Member m, Authentication a) {
        if (a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"))) return;
        String email = a.getName();
        boolean coach = m.getCoach() != null && m.getCoach().getUser().getEmail().equalsIgnoreCase(email);
        boolean self  = m.getUser() != null && m.getUser().getEmail().equalsIgnoreCase(email);
        if (!coach && !self) throw new AccessDeniedException("Forbidden");
    }

    private Member member(Long id) {
        return memberRepo.findById(id).orElseThrow(() -> new NotFoundException("Membre introuvable"));
    }
    private Member mine(Authentication a) {
        return memberRepo.findByUserEmail(a.getName())
                .orElseThrow(() -> new NotFoundException("Aucun membre lié à votre compte"));
    }
    private MeasurementResponse map(BodyMeasurement b) {
        return new MeasurementResponse(b.getId(), b.getMeasuredOn(), b.getWeightKg(), b.getBodyFatPercent(),
                b.getWaistCm(), b.getChestCm(), b.getArmCm(), b.getNotes());
    }
}
