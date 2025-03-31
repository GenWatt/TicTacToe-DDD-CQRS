package com.example.demo.domain.valueObject;

import lombok.Value;

@Value
public class Move {
    int x;
    int y;
    PlayerId playerId;

    private Move(int x, int y, PlayerId playerId) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
    }

    public static Move create(int x, int y, PlayerId playerId) {
        return new Move(x, y, playerId);
    }

    @Override
    public String toString() {
        return "Move{" +
                "x=" + x +
                ", y=" + y +
                ", playerId=" + playerId +
                '}';
    }
}
