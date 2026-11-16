package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.FreezeRequest;
import com.example.fitnessBackEnd.dto.FreezeResult;
import com.example.fitnessBackEnd.dto.FreezeRow;
import com.example.fitnessBackEnd.service.FreezeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
public class FreezeController {
    private final FreezeService service;

    @PostMapping("/members/{id}/freeze")
    public FreezeResult freeze(@PathVariable Long id, @Valid @RequestBody FreezeRequest r, Authentication a) {
        return service.freeze(id, r, a);
    }

    @PostMapping("/members/{id}/unfreeze")
    public FreezeResult unfreeze(@PathVariable Long id, Authentication a) { return service.unfreeze(id, a.getName()); }

    @GetMapping("/members/{id}/freezes")
    public List<FreezeRow> history(@PathVariable Long id) { return service.history(id); }
}