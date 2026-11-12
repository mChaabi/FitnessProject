package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.RegisterRequest;
import com.example.fitnessBackEnd.dto.UserDTO;
import com.example.fitnessBackEnd.entity.*;
import com.example.fitnessBackEnd.enums.EmailType;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.mapper.UserMapper;
import com.example.fitnessBackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UserRepository users;
    private final MemberRepository members;
    private final PlanRepository plans;
    private final CoachRepository coaches;
    private final EnrollmentRepository requests;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final EmailQueue emailQueue;

    @Transactional
    public UserDTO register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        // An existing member file is NEVER auto-linked: without email verification,
        // anyone could take over someone else's programs and measurements.
        if (users.existsByEmail(email) || members.existsByEmailIgnoreCase(email))
            throw new ConflictException("Email déjà utilisé. Déjà client ? Demandez à l'accueil de lier votre compte.");

        MembershipPlan plan = r.planId() == null ? null : plans.findById(r.planId())
                .filter(MembershipPlan::isActive)
                .orElseThrow(() -> new BadRequestException("Abonnement indisponible"));
        Coach coach = r.coachId() == null ? null : coaches.findById(r.coachId())
                .filter(Coach::isActive)
                .orElseThrow(() -> new BadRequestException("Coach indisponible"));

        User user = userMapper.toEntity(r);                  // role = MEMBER, never from the client
        user.setMotDePasse(encoder.encode(r.motDePasse()));
        user = users.save(user);

        // status stays INACTIVE (see Member.onCreate) until reception sells a plan
        Member member = members.save(Member.builder()
                .firstName(r.prenom().trim()).lastName(r.nom().trim())
                .email(email).phone(r.phone()).user(user).build());

        if (plan != null || coach != null) {
            EnrollmentRequest req = new EnrollmentRequest();
            req.setMember(member); req.setPlan(plan); req.setCoach(coach);
            req.setStatus("PENDING"); req.setCreatedAt(LocalDateTime.now());
            requests.save(req);
        }
        emailQueue.enqueue(EmailType.WELCOME, member,
                Facts.of("Abonnement souhaité", plan == null ? null : plan.getName(),
                        "Coach souhaité", coach == null ? null : coach.getUser().getPrenom() + " " + coach.getUser().getNom()),
                "WELCOME:" + member.getId());
        return userMapper.toDTO(user);
    }
}