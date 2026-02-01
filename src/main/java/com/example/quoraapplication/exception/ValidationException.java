package com.example.quoraapplication.exception;

public class ValidationException extends RuntimeException {
    private String fieldName;
    private String fieldValue;

    public ValidationException(String fieldName, String fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ValidationException(String message) {
        super(message);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}