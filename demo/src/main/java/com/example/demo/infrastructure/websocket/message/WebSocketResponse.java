package com.example.demo.infrastructure.websocket.message;

import lombok.Getter;

@Getter
public class WebSocketResponse {
    private final String type;
    private final String payload;

    public WebSocketResponse(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}
