package com.example.demo.domain.valueObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import java.io.Serializable;
import java.util.UUID;

import com.example.demo.domain.util.UuidUtil;

import jakarta.persistence.Embeddable;

@Embeddable
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@Value
public class PlayerId implements Serializable {
    private UUID id;

    public PlayerId(String id) {
        this.id = UuidUtil.fromString(id);
    }

    public static PlayerId create() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId from(String id) {
        return new PlayerId(id);
    }

    public static PlayerId from(UUID id) {
        return new PlayerId(id);
    }
}