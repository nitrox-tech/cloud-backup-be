package com.nitro.tech.cloud.service;

public class ConflictException extends RuntimeException {

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictException(String message) {
        super(message);
    }
}
