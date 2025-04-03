package com.example.demo.infrastructure.websocket.message;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
public class WebSocketResponse {
    private final String type;
    private final String payload;
    private ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketResponse(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}
