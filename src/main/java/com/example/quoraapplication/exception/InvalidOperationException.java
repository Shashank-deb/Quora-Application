package com.example.quoraapplication.exception;

public class InvalidOperationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public InvalidOperationException() {
        super("Invalid operation");
    }

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOperationException(Throwable cause) {
        super("Invalid operation", cause);
    }
}