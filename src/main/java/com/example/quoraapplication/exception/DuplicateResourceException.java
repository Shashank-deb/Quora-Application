package com.example.quoraapplication.exception;


public class DuplicateResourceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException() {
        super("Resource already exists");
    }

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourceException(Throwable cause) {
        super("Resource already exists", cause);
    }
}