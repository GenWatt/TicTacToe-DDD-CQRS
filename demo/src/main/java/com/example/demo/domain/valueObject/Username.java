package com.example.demo.domain.valueObject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Embeddable
@NoArgsConstructor(force = true)
public class Username {
    String username;

    public static Username create(String username) {
        return new Username(username);
    }

    public static Username from(String username) {
        return new Username(username);
    }

    public static Username from(Username username) {
        return new Username(username.getUsername());
    }

    @Override
    public String toString() {
        return "Username{" + "username='" + username + '\'' + '}';
    }
}
