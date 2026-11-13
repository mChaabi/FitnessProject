package com.example.fitnessBackEnd.dto;

/** matched = false: nobody recognised. The score is shown only as a percentage. */
public record FaceMatch(boolean matched, Long memberId, String memberName, int confidence, String message) {}