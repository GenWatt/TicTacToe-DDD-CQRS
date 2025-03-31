package com.example.demo.application.dto;

import java.util.UUID;

import com.example.demo.domain.valueObject.PlayerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PlayerDto {
    private UUID playerId;
    private String username;
    private PlayerType playerType;
}
