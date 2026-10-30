package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.MemberRequest;
import com.example.fitnessBackEnd.dto.MemberResponse;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository repo;

    @Transactional
    public MemberResponse create(MemberRequest r) {
        String email = r.email().trim().toLowerCase();
        if (repo.existsByEmailIgnoreCase(email)) throw new ConflictException("Email déjà utilisé");
        return toResponse(repo.save(Member.builder()
                .firstName(r.firstName().trim()).lastName(r.lastName().trim())
                .email(email).phone(r.phone()).birthDate(r.birthDate())
                .sex(r.sex()).goal(r.goal()).build()));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest r) {
        Member m = find(id);
        String email = r.email().trim().toLowerCase();
        if (repo.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new ConflictException("Email déjà utilisé");
        m.setFirstName(r.firstName().trim()); m.setLastName(r.lastName().trim());
        m.setEmail(email); m.setPhone(r.phone()); m.setBirthDate(r.birthDate());
        m.setSex(r.sex()); m.setGoal(r.goal());
        return toResponse(m);                       // @Transactional guarda los cambios solo
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> search(MemberStatus status, String keyword, int page, int size) {
        String k = keyword == null ? "" : keyword.trim();
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("lastName", "firstName"));
        return repo.search(status, k, p).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long id) { return toResponse(find(id)); }

    /** No se borra: se conserva el historial de pagos y accesos. */
    @Transactional
    public void deactivate(Long id) { find(id).setStatus(MemberStatus.INACTIVE); }

    @Transactional(readOnly = true)
    public String qrCodeOf(Long id) { return find(id).getQrCode(); }

    private Member find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Membre introuvable"));
    }

    public MemberResponse toResponse(Member m) {
        Integer age = m.getBirthDate() == null ? null
                : Period.between(m.getBirthDate(), LocalDate.now()).getYears();
        return new MemberResponse(m.getId(), m.getFirstName(), m.getLastName(), m.getEmail(),
                m.getPhone(), m.getBirthDate(), age, m.getSex(), m.getGoal(),
                m.getStatus(), m.getJoinedAt(), m.getUser() != null);
    }
}
