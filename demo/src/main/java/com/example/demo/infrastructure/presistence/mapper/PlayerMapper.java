package com.example.demo.infrastructure.presistence.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.infrastructure.presistence.entity.PlayerEntity;

@Component
public class PlayerMapper {
    public Player toDomain(PlayerEntity entity) {
        Player player = Player.reconstruct(entity.getId(), entity.getUsername(), entity.getPlayerType(),
                new ArrayList<>(), entity.getCreatedAt(), entity.getUpdatedAt());
        return player;
    }

    public PlayerEntity toEntity(Player player) {
        return PlayerEntity.builder()
                .id(player.getId())
                .username(player.getUsername())
                .createdAt(player.getCreatedAt())
                .updatedAt(player.getUpdatedAt())
                .playerType(player.getPlayerType())
                .games(new ArrayList<>())
                .build();
    }
}