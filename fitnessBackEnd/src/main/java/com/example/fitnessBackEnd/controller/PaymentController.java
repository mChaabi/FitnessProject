package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.PaymentRow;
import com.example.fitnessBackEnd.dto.RevenueSummary;
import com.example.fitnessBackEnd.entity.Payment;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.PaymentRepository;
import com.example.fitnessBackEnd.service.ReceiptPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PaymentController {
    private final PaymentRepository repo;
    private final ReceiptPdfService pdf;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly = true)
    public Page<PaymentRow> list(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "paidAt"));
        return repo.findPage(p).map(x -> new PaymentRow(x.getId(), x.getReceiptNumber(),
                x.getSubscription().getMember().getFirstName() + " " + x.getSubscription().getMember().getLastName(),
                x.getSubscription().getPlan().getName(), x.getAmount(), x.getMethod(), x.getPaidAt()));
    }

    @GetMapping("/summary") @PreAuthorize("hasRole('ADMIN')")
    public RevenueSummary summary() {
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        return new RevenueSummary(
                repo.sumBetween(first.atStartOfDay(), first.plusMonths(1).atStartOfDay()),
                repo.sumBetween(first.minusMonths(1).atStartOfDay(), first.atStartOfDay()));
    }

    @GetMapping(value = "/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTION')") @Transactional(readOnly = true)
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) {
        Payment p = repo.findDetailed(id).orElseThrow(() -> new NotFoundException("Reçu introuvable"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + p.getReceiptNumber() + ".pdf")
                .body(pdf.generate(p));
    }
}
