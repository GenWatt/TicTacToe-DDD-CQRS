package com.example.demo.application.dto;

import java.util.List;
import java.util.UUID;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.Move;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameDto {
    private Board board;
    private List<UUID> playerIds;
    private UUID gameId;
    private List<Move> moves;
}
