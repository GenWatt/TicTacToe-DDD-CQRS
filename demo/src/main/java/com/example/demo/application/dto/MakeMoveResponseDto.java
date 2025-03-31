package com.example.demo.application.dto;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MakeMoveResponseDto {
    private Board board;
    private Move lastMove;
    private PlayerId nextPlayer;
}