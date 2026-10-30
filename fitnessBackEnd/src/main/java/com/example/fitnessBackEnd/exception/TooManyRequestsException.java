package com.example.fitnessBackEnd.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {
    private final long retryAfterSeconds;
    public TooManyRequestsException(long retryAfterSeconds) {
        super("Trop de tentatives");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
