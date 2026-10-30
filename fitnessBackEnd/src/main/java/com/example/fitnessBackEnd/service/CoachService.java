package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.CoachRequest;
import com.example.fitnessBackEnd.dto.CoachResponse;
import com.example.fitnessBackEnd.dto.MemberResponse;
import com.example.fitnessBackEnd.entity.Coach;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.CoachRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachRepository coachRepo;
    private final MemberRepository memberRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final MemberService memberService;

    @Transactional
    public CoachResponse create(CoachRequest r) {
        String email = r.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) throw new ConflictException("Email déjà utilisé");
        User u = userRepo.save(User.builder().nom(r.nom().trim()).prenom(r.prenom().trim())
                .email(email).motDePasse(encoder.encode(r.motDePasse()))
                .role(Role.COACH).enabled(true).build());
        Coach c = coachRepo.save(Coach.builder().user(u).specialties(r.specialties())
                .bio(r.bio()).active(true).build());
        return map(c, 0);
    }

    @Transactional(readOnly = true)
    public List<CoachResponse> list() {
        return coachRepo.findAllWithUser().stream()
                .map(c -> map(c, memberRepo.countByCoachId(c.getId()))).toList();   // pocos coachs: aceptable
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Coach c = find(id);
        c.setActive(active);
        c.getUser().setEnabled(active);
        if (!active) memberRepo.clearCoach(id);
    }

    @Transactional
    public void assign(Long memberId, Long coachId) {
        Member m = memberRepo.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        if (coachId == null) { m.setCoach(null); return; }
        Coach c = find(coachId);
        if (!c.isActive()) throw new ConflictException("Ce coach est désactivé");
        m.setCoach(c);
    }

    @Transactional
    public void linkAccount(Long memberId, String email) {
        Member m = memberRepo.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        User u = userRepo.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Aucun compte avec cet email"));
        if (u.getRole() != Role.MEMBER) throw new ConflictException("Ce compte n'est pas un compte membre");
        if (memberRepo.existsByUserId(u.getId())) throw new ConflictException("Compte déjà lié à un membre");
        if (m.getUser() != null) throw new ConflictException("Ce membre a déjà un compte");
        m.setUser(u);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> clientsOf(String coachEmail, int page, int size) {
        Coach c = coachRepo.findByUserEmail(coachEmail).orElseThrow(() -> new AccessDeniedException("Not a coach"));
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("lastName", "firstName"));
        return memberRepo.findByCoachId(c.getId(), p).map(memberService::toResponse);
    }

    private Coach find(Long id) { return coachRepo.findById(id).orElseThrow(() -> new NotFoundException("Coach introuvable")); }
    private CoachResponse map(Coach c, long clients) {
        User u = c.getUser();
        return new CoachResponse(c.getId(), u.getNom(), u.getPrenom(), u.getEmail(),
                c.getSpecialties(), c.getBio(), c.isActive(), clients);
    }
}
