package com.example.demo.domain.aggregate;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.domain.exception.UsernameException;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.PlayerType;
import com.example.demo.domain.valueObject.Username;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends AggregateRoot<PlayerId> {
    private Username username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Game> games;
    private PlayerType playerType;
    private String password;

    private Player(PlayerId playerId, Username username, String password) {
        super(playerId, 1);

        if (playerId == null) {
            throw new IllegalArgumentException("PlayerId cannot be null");
        }

        if (username == null) {
            throw new UsernameException("Username cannot be null");
        }

        if (username.getUsername().length() > 50) {
            throw new UsernameException("Username cannot be longer than 50 characters");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.games = List.of();
        this.username = username;
        this.password = password;
    }

    private Player(PlayerId playerId, Username username, PlayerType playerType, String password, List<Game> games,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(playerId, 1);
        this.username = username;
        this.playerType = playerType;
        this.password = password;
        this.games = games;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Player create(Username username, String password) {
        return new Player(PlayerId.create(), username, password);
    }

    public static Player reconstruct(PlayerId playerId, Username username, PlayerType playerType, String password,
            List<Game> games,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Player(playerId, username, playerType, password, games, createdAt, updatedAt);
    }
}
