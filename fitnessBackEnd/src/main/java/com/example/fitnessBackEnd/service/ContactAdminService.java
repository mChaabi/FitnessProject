package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.ContactRow;
import com.example.fitnessBackEnd.entity.Contact;
import com.example.fitnessBackEnd.enums.ContactStatus;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactAdminService {

    private final ContactRepository repo;

    @Transactional(readOnly = true)
    public Page<ContactRow> list(ContactStatus status, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return (status == null ? repo.findAll(p) : repo.findByStatus(status, p)).map(this::row);
    }

    @Transactional(readOnly = true)
    public Map<ContactStatus, Long> stats() {
        Map<ContactStatus, Long> out = new EnumMap<>(ContactStatus.class);
        for (ContactStatus s : ContactStatus.values()) out.put(s, repo.countByStatus(s));
        return out;
    }

    @Transactional
    public ContactRow setStatus(Long id, ContactStatus status, String staff) {
        Contact c = repo.findById(id).orElseThrow(() -> new NotFoundException("Message introuvable"));
        c.setStatus(status);
        c.setHandledBy(status == ContactStatus.NEW ? null : staff);
        c.setHandledAt(status == ContactStatus.NEW ? null : LocalDateTime.now());
        return row(c);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Message introuvable");
        repo.deleteById(id);
    }

    private ContactRow row(Contact c) {
        return new ContactRow(c.getId(), c.getNom(), c.getEmail(), c.getSujet(), c.getMessage(),
                c.getStatus(), c.getCreatedAt(), c.getHandledBy(), c.getHandledAt());
    }
}