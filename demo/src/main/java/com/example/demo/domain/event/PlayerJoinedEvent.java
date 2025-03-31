package com.example.demo.domain.event;

import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class PlayerJoinedEvent extends DomainEvent {
    private final PlayerId playerId;

    public PlayerJoinedEvent(PlayerId playerId) {
        super();
        this.playerId = playerId;
    }
}