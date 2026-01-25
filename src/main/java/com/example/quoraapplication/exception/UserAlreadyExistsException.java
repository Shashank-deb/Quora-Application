package com.example.quoraapplication.exception;

/**
 * Custom exception thrown when attempting to create a user that already exists
 * Used during user registration when username or email already exists
 */
public class UserAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public UserAlreadyExistsException() {
        super("User already exists");
    }

    /**
     * Constructor with custom message
     * @param message - the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * @param message - the detail message
     * @param cause - the cause of the exception
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause
     * @param cause - the cause of the exception
     */
    public UserAlreadyExistsException(Throwable cause) {
        super("User already exists", cause);
    }
}