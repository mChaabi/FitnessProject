package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // Inside com.example.fitnessBackEnd.repository.UserRepository.java

    Optional<User> findByEmailIgnoreCase(String email);
}
