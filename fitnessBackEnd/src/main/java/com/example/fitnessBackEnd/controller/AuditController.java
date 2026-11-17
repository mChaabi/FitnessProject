package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.AuditRow;
import com.example.fitnessBackEnd.entity.AuditEntry;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditRepository repo;

    @GetMapping @Transactional(readOnly = true)
    public Page<AuditRow> list(@RequestParam(defaultValue = "") String type,
                               @RequestParam(defaultValue = "") String q,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "25") int size) {
        LocalDate[] r = range(from, to);
        var p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return repo.search(r[0].atStartOfDay(), r[1].plusDays(1).atStartOfDay(), type.trim(), q.trim(), p).map(this::row);
    }

    @GetMapping("/stats") @Transactional(readOnly = true)
    public Map<String, Long> stats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] r = range(from, to);
        Map<String, Long> out = new HashMap<>();
        for (Object[] row : repo.countByType(r[0].atStartOfDay(), r[1].plusDays(1).atStartOfDay()))
            out.put((String) row[0], (Long) row[1]);
        return out;
    }

    /** Default: the last 30 days. A range is limited to one year. */
    private LocalDate[] range(LocalDate from, LocalDate to) {
        LocalDate t = to == null ? LocalDate.now() : to;
        LocalDate f = from == null ? t.minusDays(29) : from;
        if (f.isAfter(t) || ChronoUnit.DAYS.between(f, t) > 366) throw new BadRequestException("Période invalide (1 an maximum)");
        return new LocalDate[]{f, t};
    }

    private AuditRow row(AuditEntry a) {
        return new AuditRow(a.getId(), a.getCreatedAt(), a.getActor(), a.getActorRole(),
                a.getAction(), a.getEntityType(), a.getEntityId(), a.getSummary());
    }
}