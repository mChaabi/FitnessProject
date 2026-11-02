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
        userRepository.findByEmailIgnoreCase(adminEmail).ifPresentOrElse(u -> {
            u.setMotDePasse(passwordEncoder.encode("123456789"));
            u.setEnabled(true);
            userRepository.save(u);
        }, () -> userRepository.save(User.builder().nom("Admin").prenom("System")
                .email(adminEmail).motDePasse(passwordEncoder.encode("123456789"))
                .role(Role.ADMIN).enabled(true).build()));
    }
}
