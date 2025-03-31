package com.example.demo.infrastructure.websocket.message;

import java.util.UUID;

import lombok.Getter;

@Getter
public class PlayMoveMessage extends MessageBase {
    private UUID gameId;
    private UUID playerId;

    private int x;
    private int y;

    public PlayMoveMessage() {
        super(MessageType.PLAY_MOVE);
    }
}
