package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.*;
import com.example.fitnessBackEnd.service.BookingService;
import com.example.fitnessBackEnd.service.ClassScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ClassController {

    private final ClassScheduleService schedule;
    private final BookingService booking;
    @Value("${app.classes.generate-days:28}") private int horizon;

    @GetMapping("/sessions")
    public List<SessionResponse> sessions(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                          Authentication a) { return schedule.list(from, to, a); }

    @PostMapping("/sessions") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SessionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule.createSession(r)); }

    @PostMapping("/sessions/{id}/cancel") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelSession(@PathVariable Long id) { booking.cancelSession(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/sessions/{id}/bookings") @PreAuthorize("hasAnyRole('ADMIN','RECEPTION','COACH')")
    public List<AttendeeRow> attendees(@PathVariable Long id, Authentication a) { return booking.attendees(id, a); }

    @PostMapping("/sessions/{id}/book") @PreAuthorize("hasAnyRole('MEMBER','ADMIN','RECEPTION')")
    public ResponseEntity<BookingResponse> book(@PathVariable Long id, @RequestBody(required = false) BookRequest r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(booking.book(id, r == null ? null : r.memberId(), a)); }

    @PostMapping("/bookings/{id}/cancel") @PreAuthorize("hasAnyRole('MEMBER','ADMIN','RECEPTION')")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication a) { booking.cancel(id, a); return ResponseEntity.noContent().build(); }

    @GetMapping("/templates") @PreAuthorize("hasRole('ADMIN')")
    public List<TemplateResponse> templates() { return schedule.listTemplates(); }

    @PostMapping("/templates") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule.saveTemplate(null, r)); }

    @PutMapping("/templates/{id}") @PreAuthorize("hasRole('ADMIN')")
    public TemplateResponse updateTemplate(@PathVariable Long id, @Valid @RequestBody TemplateRequest r) { return schedule.saveTemplate(id, r); }

    @PostMapping("/generate") @PreAuthorize("hasRole('ADMIN')")
    public GenerateResult generate() { return new GenerateResult(schedule.generate(LocalDate.now(), LocalDate.now().plusDays(horizon))); }
}
