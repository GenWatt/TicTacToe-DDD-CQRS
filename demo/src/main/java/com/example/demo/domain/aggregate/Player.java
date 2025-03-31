package com.example.demo.domain.aggregate;

import java.time.LocalDateTime;
import java.util.List;

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

    private Player(PlayerId playerId, Username username) {
        super(playerId, 1);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.games = List.of();
        this.username = username;
    }

    public static Player create(Username username) {
        return new Player(PlayerId.create(), username);
    }

    public static Player reconstruct(PlayerId playerId, Username username) {
        return new Player(playerId, username);
    }
}
