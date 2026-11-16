package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.entity.Contact;
import com.example.fitnessBackEnd.enums.ContactStatus;
import com.example.fitnessBackEnd.mapper.ContactMapper;
import com.example.fitnessBackEnd.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Set<String> SUBJECTS = Set.of("info", "coach", "tech");

    private final ContactRepository repository;
    private final ContactMapper mapper;

    public ContactDTO saveMessage(ContactDTO dto) {
        // honeypot: a bot filled the hidden field. Answer "success" so it learns nothing, and store nothing.
        if (dto.website() != null && !dto.website().isBlank()) return dto;

        Contact c = mapper.toEntity(dto);
        if (c.getSujet() != null && !SUBJECTS.contains(c.getSujet())) c.setSujet(null);   // whitelist
        c.setStatus(ContactStatus.NEW);
        c.setCreatedAt(LocalDateTime.now());
        return mapper.toDTO(repository.save(c));
    }
}