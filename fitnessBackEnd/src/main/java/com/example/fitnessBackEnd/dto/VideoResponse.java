package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.VideoCategory;

public record VideoResponse(Long id, String title, String description, VideoCategory category,
                            String type, String source, Integer durationMin, boolean active) {}