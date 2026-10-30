package com.example.fitnessBackEnd.config;

import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import com.example.fitnessBackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@fitness.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .nom("Admin")
                    .prenom("System")
                    .email(adminEmail)
                    .motDePasse(passwordEncoder.encode("123456789"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Usuario Admin creado exitosamente: " + adminEmail);
        }
    }
}
