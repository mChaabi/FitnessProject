package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.Sex;

import java.time.LocalDate;

public record MemberResponse(
        Long id, String firstName, String lastName, String email, String phone,
        LocalDate birthDate, Integer age, Sex sex, String goal,
        MemberStatus status, LocalDate joinedAt, boolean hasAccount) {}
