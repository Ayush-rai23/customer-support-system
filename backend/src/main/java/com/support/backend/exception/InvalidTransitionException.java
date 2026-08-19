package com.support.backend.exception;

/** Thrown when a requested ticket status transition is not allowed; mapped to HTTP 409. */
public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String message) {
        super(message);
    }
}
