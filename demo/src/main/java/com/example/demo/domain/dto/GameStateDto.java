package com.example.demo.domain.dto;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDto {
    private List<PlayerId> players;
    private Board board;
    private List<Move> moves;
    private GameState state;
    private PlayerId nextPlayer;
    private PlayerId winner;
}