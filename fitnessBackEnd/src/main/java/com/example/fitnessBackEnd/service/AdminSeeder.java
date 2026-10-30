package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import com.example.fitnessBackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}")    private String email;
    @Value("${app.admin.password:}") private String password;

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank() || repo.existsByEmail(email)) return;
        repo.save(User.builder()
                .nom("Admin").prenom("Fitforge")
                .email(email).motDePasse(encoder.encode(password))
                .role(Role.ADMIN).enabled(true).build());
    }
}