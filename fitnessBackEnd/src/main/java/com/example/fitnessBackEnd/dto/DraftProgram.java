package com.example.fitnessBackEnd.dto;

import java.util.List;

public record DraftProgram(String name, String goal, List<DraftItem> items) {}
