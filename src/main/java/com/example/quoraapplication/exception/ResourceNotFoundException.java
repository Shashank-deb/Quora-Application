package com.example.quoraapplication.exception;

/**
 * Custom exception thrown when a requested resource is not found
 * Used throughout the application for 404 Not Found scenarios
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    /**
     * Constructor with custom message
     * @param message - the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * @param message - the detail message
     * @param cause - the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause
     * @param cause - the cause of the exception
     */
    public ResourceNotFoundException(Throwable cause) {
        super("Resource not found", cause);
    }
}