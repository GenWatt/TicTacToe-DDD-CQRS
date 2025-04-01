package com.example.demo.domain.repository;

import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;

import io.smallrye.mutiny.Uni;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository {
    Uni<Game> findById(GameId id);

    Uni<Game> findInProgressGameByPlayerId(PlayerId playerId);

    Uni<List<Game>> findAll();

    Uni<Game> save(Game game);
}