package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.MemberOverview;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.service.MemberOverviewService;
import com.example.fitnessBackEnd.service.QrGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MeController {

    private final MemberOverviewService overview;
    private final MemberRepository members;

    @GetMapping("/overview")
    public MemberOverview overview(Authentication a) { return overview.mine(a.getName()); }

    /** The member's own access QR (shown on the phone at the entrance). */
    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(Authentication a) throws Exception {
        Member m = members.findByUserEmail(a.getName())
                .orElseThrow(() -> new NotFoundException("Aucun membre lié à votre compte"));
        return QrGenerator.png(m.getQrCode(), 320);
    }
}