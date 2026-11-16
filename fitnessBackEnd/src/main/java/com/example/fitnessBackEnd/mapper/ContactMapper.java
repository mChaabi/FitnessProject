package com.example.fitnessBackEnd.mapper;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.entity.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public ContactDTO toDTO(Contact e) {
        return new ContactDTO(e.getId(), e.getNom(), e.getEmail(), e.getSujet(), e.getMessage(), null);
    }

    /** Never copies the id: a client must not be able to overwrite an existing message. */
    public Contact toEntity(ContactDTO d) {
        return Contact.builder()
                .nom(d.nom().trim()).email(d.email().trim().toLowerCase())
                .sujet(d.sujet()).message(d.message().trim())
                .build();
    }
}