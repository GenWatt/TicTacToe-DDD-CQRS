package com.example.demo.domain.exception;

public class GameException extends DomainException {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
