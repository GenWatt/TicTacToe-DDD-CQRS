package com.example.demo.domain.service;

import com.example.demo.domain.dto.GameStateDto;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import io.smallrye.mutiny.Uni;

public interface GameService {
    /**
     * Process a player move in a game
     * 
     * @param gameId   The ID of the game
     * @param playerId The ID of the player making the move
     * @param move     The move to make
     * @return Uni<Void> indicating completion
     */
    Uni<Void> makeMove(GameId gameId, PlayerId playerId, Move move);

    /**
     * Get the game state for a specific game
     * 
     * @param gameId The ID of the game
     * @return Uni<GameStateDto> with the current game state
     */
    Uni<GameStateDto> getGameState(GameId gameId);

    /**
     * Check if a player is part of a game
     * 
     * @param gameId   The ID of the game
     * @param playerId The ID of the player
     * @return Uni<Boolean> indicating if player is in game
     */
    Uni<Boolean> isPlayerInGame(GameId gameId, PlayerId playerId);
}