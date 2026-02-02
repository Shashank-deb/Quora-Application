package com.example.quoraapplication.exception;

/**
 * Thrown when user lacks required permissions
 * HTTP 403 Forbidden
 */
public class UnauthorizedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Access denied");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super("Access denied", cause);
    }
}