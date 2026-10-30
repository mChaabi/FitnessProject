package com.example.fitnessBackEnd.mapper;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.entity.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public ContactDTO toDTO(Contact entity) {
        return new ContactDTO(
                entity.getId(),
                entity.getNom(),
                entity.getEmail(),
                entity.getMessage()
        );
    }

    public Contact toEntity(ContactDTO dto) {
        return Contact.builder()
                .id(dto.id())
                .nom(dto.nom())
                .email(dto.email())
                .message(dto.message())
                .build();
    }
}
