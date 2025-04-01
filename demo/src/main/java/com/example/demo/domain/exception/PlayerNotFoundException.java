package com.example.demo.domain.exception;

import com.example.demo.domain.valueObject.Username;

public class PlayerNotFoundException extends DomainException {
    public PlayerNotFoundException() {
        super("Player Not Found.");
    }

    public PlayerNotFoundException(Username username) {
        super("Player Not Found. " + username.getUsername());
    }

    public PlayerNotFoundException(String message) {
        super("Player Not Found." + message);
    }
}
