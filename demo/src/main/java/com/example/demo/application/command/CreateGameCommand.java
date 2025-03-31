package com.example.demo.application.command;

import java.util.List;

import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGameCommand {
    private GameId gameId;
    private List<PlayerId> players;
}