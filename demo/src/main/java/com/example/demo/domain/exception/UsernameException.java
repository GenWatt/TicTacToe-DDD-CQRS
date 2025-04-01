package com.example.demo.domain.exception;

public class UsernameException extends DomainException {
    public UsernameException() {
        super("Username is not valid.");
    }

    public UsernameException(String message) {
        super(message);
    }
}
