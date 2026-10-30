package com.example.fitnessBackEnd.exception;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp
) {
    // Constructor para aceptar los 3 argumentos que usas en el GlobalExceptionHandler
    public ApiError(int status, String message, Object details) {
        this(status, message, LocalDateTime.now());
    }
}