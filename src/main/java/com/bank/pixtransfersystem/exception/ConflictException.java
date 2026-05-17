package com.bank.pixtransfersystem.exception;

public class ConflictException extends RuntimeException {

    private final Object existingData;

    public ConflictException(String message, Object existingData) {
        super(message);
        this.existingData = existingData;
    }

    public Object getExistingData() {
        return existingData;
    }
}
