package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.ContactRow;
import com.example.fitnessBackEnd.enums.ContactStatus;
import com.example.fitnessBackEnd.service.ContactAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/messages")           // /admin/** is already ADMIN-only in SecurityConfig
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ContactAdminController {

    private final ContactAdminService service;

    @GetMapping
    public Page<ContactRow> list(@RequestParam(required = false) ContactStatus status,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        return service.list(status, page, size);
    }

    @GetMapping("/stats")
    public Map<ContactStatus, Long> stats() { return service.stats(); }

    @PatchMapping("/{id}/status")
    public ContactRow setStatus(@PathVariable Long id, @RequestParam ContactStatus value, Authentication a) {
        return service.setStatus(id, value, a.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}