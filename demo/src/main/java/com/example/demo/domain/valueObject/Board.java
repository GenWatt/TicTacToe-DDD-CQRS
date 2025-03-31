package com.example.demo.domain.valueObject;

import lombok.NoArgsConstructor;
import lombok.Value;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Arrays;

@Value
@Embeddable
@NoArgsConstructor(force = true)
public class Board implements Serializable {
    PlayerId[][] board;
    private static final int SIZE = 3;

    public Board(PlayerId[][] board) {
        this.board = board;
    }

    public static Board empty() {
        PlayerId[][] emptyBoard = new PlayerId[SIZE][SIZE];
        return new Board(emptyBoard);
    }

    public Board makeMove(int x, int y, PlayerId playerId) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Position is outside the board");
        }

        if (board[x][y] != null) {
            throw new IllegalArgumentException("Position already occupied");
        }

        PlayerId[][] newGrid = deepCopy(board);
        newGrid[x][y] = playerId;
        return new Board(newGrid);
    }

    public PlayerId checkWinner() {
        // Check rows
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][0].equals(board[i][2])) {
                return board[i][0];
            }
        }

        // Check columns
        for (int i = 0; i < SIZE; i++) {
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[0][i].equals(board[2][i])) {
                return board[0][i];
            }
        }

        // Check diagonals
        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[0][0].equals(board[2][2])) {
            return board[0][0];
        }

        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[0][2].equals(board[2][0])) {
            return board[0][2];
        }

        return null;
    }

    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private PlayerId[][] deepCopy(PlayerId[][] original) {
        PlayerId[][] copy = new PlayerId[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            copy[i] = Arrays.copyOf(original[i], SIZE);
        }
        return copy;
    }
}