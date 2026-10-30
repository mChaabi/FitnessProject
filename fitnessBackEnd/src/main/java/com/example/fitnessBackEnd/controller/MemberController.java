package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.MemberRequest;
import com.example.fitnessBackEnd.dto.MemberResponse;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.service.MemberService;
import com.example.fitnessBackEnd.service.QrGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MemberController {

    private final MemberService service;

    @GetMapping
    public Page<MemberResponse> list(@RequestParam(required = false) MemberStatus status,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return service.search(status, q, page, size);
    }

    @GetMapping("/{id}")
    public MemberResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")                       // solo el dueño desactiva
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable Long id) throws Exception {
        return QrGenerator.png(service.qrCodeOf(id), 320);
    }
}
