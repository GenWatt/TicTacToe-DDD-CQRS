package com.example.demo.infrastructure.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PlayerModel {
    private String id;
    private String username;
}
