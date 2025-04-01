package com.example.demo.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.PlayerId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPlayerQuery implements Query<Player> {
    private PlayerId playerId;
}