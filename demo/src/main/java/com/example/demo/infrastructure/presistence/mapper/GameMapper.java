package com.example.demo.infrastructure.presistence.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.presistence.entity.GameEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GameMapper {

    public Game toDomain(GameEntity entity) {
        if (entity == null) {
            return null;
        }

        List<PlayerId> playerIds = entity.getPlayers().stream()
                .map(playerEntity -> playerEntity.getId())
                .collect(Collectors.toList());

        return Game.reconstruct(
                entity.getId(),
                entity.getState(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                playerIds,
                entity.getMoves() != null ? entity.getMoves() : Collections.emptyList(),
                entity.getBoard(),
                entity.getCurrentPlayerMoveId(),
                entity.getWinnerId());
    }

    public GameEntity toEntity(Game game) {
        if (game == null) {
            return null;
        }

        GameEntity gameEntity = GameEntity.builder()
                .id(game.getId())
                .state(game.getState())
                .createdAt(game.getCreatedAt())
                .updatedAt(game.getUpdatedAt())
                .winnerId(game.getWinner())
                .currentPlayerMoveId(game.getCurrentPlayerMoveId())
                .moves(game.getMoves() != null ? game.getMoves() : Collections.emptyList())
                .board(game.getBoard())
                .build();

        return gameEntity;
    }
}