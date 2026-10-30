package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.entity.Contact;
import com.example.fitnessBackEnd.mapper.ContactMapper;
import com.example.fitnessBackEnd.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository repository;
    private final ContactMapper mapper;

    public ContactDTO saveMessage(ContactDTO dto) {
        Contact entity = mapper.toEntity(dto);
        Contact saved = repository.save(entity);
        return mapper.toDTO(saved);
    }
}