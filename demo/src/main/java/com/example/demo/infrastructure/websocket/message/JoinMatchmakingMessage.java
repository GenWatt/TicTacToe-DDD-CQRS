package com.example.demo.infrastructure.websocket.message;

import lombok.Getter;

@Getter
public class JoinMatchmakingMessage extends MessageBase {
    private String playerId;

    public JoinMatchmakingMessage() {
        super(MessageType.JOIN_MATCHMAKING);
    }
}