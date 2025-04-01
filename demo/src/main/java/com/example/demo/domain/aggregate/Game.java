package com.example.demo.domain.aggregate;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.domain.event.GameCreatedEvent;
import com.example.demo.domain.event.GameEndedByDisconnectionEvent;
import com.example.demo.domain.event.GameEndedEvent;
import com.example.demo.domain.event.MovePlayedEvent;
import com.example.demo.domain.exception.GameException;
import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;

@Getter
public class Game extends AggregateRoot<GameId> {
    private GameState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlayerId> playerIds;
    private PlayerId winner;
    private List<Move> moves;
    private Board board;
    private PlayerId currentPlayerMoveId;

    private Game(GameId id, List<PlayerId> playerIds) {
        super(id, 1);
        if (playerIds.size() != 2) {
            throw new GameException("Game must have exactly 2 players at creation.");
        }

        this.state = GameState.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.playerIds = playerIds;
        this.moves = new ArrayList<>();
        this.board = Board.empty();
        this.currentPlayerMoveId = playerIds.get(0);

        registerEvent(new GameCreatedEvent(id, playerIds));
    }

    public static Game create(GameId id, List<PlayerId> players) {
        return new Game(id, players);
    }

    public static Game reconstruct(GameId id, GameState state, LocalDateTime createdAt,
            LocalDateTime updatedAt, List<PlayerId> players,
            List<Move> moves, Board board, PlayerId currentPlayerMoveId, PlayerId winner) {

        Game game = new Game(id, players);
        game.state = state;
        game.createdAt = createdAt;
        game.updatedAt = updatedAt;
        game.moves = moves;
        game.board = board;
        game.currentPlayerMoveId = currentPlayerMoveId;
        game.playerIds = players;
        game.winner = winner;

        return game;
    }

    public void playMove(PlayerId playerId, Move move) {
        if (state != GameState.IN_PROGRESS) {
            throw new GameException("Game is not in progress.");
        }

        if (!playerIds.contains(playerId)) {
            throw new GameException("Player is not part of this game.");
        }

        if (!currentPlayerMoveId.equals(playerId)) {
            throw new GameException("It's not your turn.");
        }

        this.board = board.makeMove(move.getX(), move.getY(), playerId);
        moves.add(move);
        updatedAt = LocalDateTime.now();
        currentPlayerMoveId = playerIds.get((playerIds.indexOf(currentPlayerMoveId) + 1) % playerIds.size());
        registerEvent(new MovePlayedEvent(getId(), playerId, currentPlayerMoveId, playerIds, move, board));

        this.winner = board.checkWinner();
        if (winner != null || board.isFull()) {
            endGame(winner);
        }
    }

    public void playerDisconnect(PlayerId disconnectedPlayerId) {
        if (state != GameState.IN_PROGRESS) {
            return; // Game already ended
        }

        if (!playerIds.contains(disconnectedPlayerId)) {
            throw new GameException("Player is not part of this game.");
        }

        // Find the other player (who did not disconnect)
        PlayerId remainingPlayer = playerIds.stream()
                .filter(playerId -> !playerId.equals(disconnectedPlayerId))
                .findFirst()
                .orElseThrow(() -> new GameException("Invalid game state: could not find remaining player"));

        // Set the remaining player as the winner
        this.winner = remainingPlayer;

        // End the game with disconnection reason
        endGameWithDisconnection(disconnectedPlayerId, remainingPlayer);
    }

    private void endGameWithDisconnection(PlayerId disconnectedPlayerId, PlayerId remainingPlayer) {
        if (state != GameState.IN_PROGRESS) {
            return;
        }

        state = GameState.ENDED;
        updatedAt = LocalDateTime.now();

        registerEvent(new GameEndedByDisconnectionEvent(remainingPlayer, this, disconnectedPlayerId));
    }

    private void endGame(PlayerId winner) {
        if (state != GameState.IN_PROGRESS) {
            return;
        }

        state = GameState.ENDED;
        updatedAt = LocalDateTime.now();

        registerEvent(new GameEndedEvent(winner, this));
    }
}
