package com.example.fitnessBackEnd.mapper;

import com.example.fitnessBackEnd.dto.RegisterRequest;
import com.example.fitnessBackEnd.dto.UserDTO;
import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDTO(User u) {
        return new UserDTO(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getRole());
    }

    public User toEntity(RegisterRequest r) {
        return User.builder()
                .nom(r.nom().trim()).prenom(r.prenom().trim())
                .email(r.email().trim().toLowerCase())
                .motDePasse(r.motDePasse())          // el service lo hashea
                .role(Role.MEMBER)                   // nunca viene del cliente
                .enabled(true)
                .build();
    }
}