package com.example.demo.application.dto;

import java.util.List;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.Move;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchFoundResponseDto {
    private String gameId;
    private GameState gameState;
    private PlayerDto you;
    private PlayerDto opponent;
    private Board board;
    private List<Move> moves;
    private boolean yourTurn;
}