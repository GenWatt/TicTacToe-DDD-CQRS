package com.example.demo.domain.valueObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.UUID;

import com.example.demo.domain.util.UuidUtil;

import jakarta.persistence.Embeddable;

@Value
@Embeddable
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
public class GameId {
    private UUID id;

    public GameId(String id) {
        this.id = UuidUtil.fromString(id);
    }

    public static GameId create() {
        return new GameId(UUID.randomUUID());
    }

    public static GameId from(String id) {
        return new GameId(id);
    }

    public static GameId from(UUID id) {
        return new GameId(id);
    }
}