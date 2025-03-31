package com.example.demo.infrastructure.websocket;

import java.time.LocalDateTime;

import com.example.demo.domain.valueObject.GameState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameModel {
    private String gameId;
    private GameState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PlayerModel player1;
    private PlayerModel player2;
    private String yourPlayerId;
    private String playerTurn;
}