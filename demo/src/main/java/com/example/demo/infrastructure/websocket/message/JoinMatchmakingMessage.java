package com.example.demo.infrastructure.websocket.message;

import java.util.UUID;

import lombok.Getter;

@Getter
public class JoinMatchmakingMessage extends MessageBase {
    private UUID playerId;

    public JoinMatchmakingMessage() {
        super(MessageType.JOIN_MATCHMAKING);
    }
}