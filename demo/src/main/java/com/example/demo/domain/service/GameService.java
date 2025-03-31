package com.example.demo.domain.service;

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
    Uni<Void> makeMove(GameId gameId, PlayerId playerId, Move move);;

    /**
     * Check if a player is part of a game
     * 
     * @param gameId   The ID of the game
     * @param playerId The ID of the player
     * @return Uni<Boolean> indicating if player is in game
     */
    Uni<Boolean> isPlayerInGame(GameId gameId, PlayerId playerId);
}