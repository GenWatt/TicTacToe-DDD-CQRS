package com.example.demo.application.dto;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameEndedResponseDto {
    private Board board;
    private PlayerId winner; // Null means draw
    private GameState gameState;
}