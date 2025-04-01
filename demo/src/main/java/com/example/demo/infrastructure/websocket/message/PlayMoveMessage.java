package com.example.demo.infrastructure.websocket.message;

import lombok.Getter;

@Getter
public class PlayMoveMessage extends MessageBase {
    private String gameId;
    private String playerId;

    private int x;
    private int y;

    public PlayMoveMessage() {
        super(MessageType.PLAY_MOVE);
    }
}
