package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Contact;
import com.example.fitnessBackEnd.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Spring genera esto automáticamente por el nombre del método:
    // SELECT * FROM contacts WHERE email = ?
    List<Contact> findByEmail(String email);

    // Opcional: Buscar contactos por nombre que contenga una palabra
    List<Contact> findByNomContainingIgnoreCase(String nom);

    Page<Contact> findByStatus(ContactStatus status, Pageable pageable);
    long countByStatus(ContactStatus status);
}