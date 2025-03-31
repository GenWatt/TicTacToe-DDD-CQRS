package com.example.demo.api.mapper;

import com.example.demo.api.dto.PlayerResponse;
import com.example.demo.domain.aggregate.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerDtoMapper {

    public PlayerResponse toResponse(Player player) {
        return PlayerResponse.builder()
                .id(player.getId().getId())
                .username(player.getUsername().getUsername())
                .build();
    }
}