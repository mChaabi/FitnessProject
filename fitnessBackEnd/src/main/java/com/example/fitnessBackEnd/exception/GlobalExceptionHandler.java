package com.example.fitnessBackEnd.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Captura errores genéricos o personalizados
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiError> bad(BadRequestException e) {
        return ResponseEntity.badRequest().body(new ApiError(400, e.getMessage(), null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(new ApiError(409, "Opération impossible : élément utilisé ailleurs", null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiError> unauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401).body(new ApiError(401, e.getMessage(), null));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ResponseEntity<ApiError> tooMany(TooManyRequestsException e) {
        long minutes = Math.max(1, (e.getRetryAfterSeconds() + 59) / 60);
        return ResponseEntity.status(429)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .body(new ApiError(429, "Trop de tentatives. Réessayez dans " + minutes + " min.", null));
    }

    // IMPRESCINDIBLE antes del manejador final: sin él, un 403 de @PreAuthorize se convertiría en 500
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(AccessDeniedException e) {
        return ResponseEntity.status(403).body(new ApiError(403, "Accès refusé", null));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> unreadable(Exception e) {
        return ResponseEntity.badRequest().body(new ApiError(400, "Requête invalide", null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> fallback(Exception e) {
        if (e instanceof ErrorResponse er) {                 // 404, 405, 415... generados por Spring MVC
            int status = er.getStatusCode().value();
            String msg = status == 404 ? "Ressource introuvable" : "Requête invalide";
            return ResponseEntity.status(status).headers(er.getHeaders()).body(new ApiError(status, msg, null));
        }
        String ref = UUID.randomUUID().toString().substring(0, 8);
        log.error("Erreur non gérée [ref={}]", ref, e);       // el detalle va al log, nunca al cliente
        return ResponseEntity.status(500).body(new ApiError(500, "Erreur interne. Référence : " + ref, null));
    }
}