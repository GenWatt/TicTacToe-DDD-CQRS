package com.example.demo.infrastructure.presistence.converter;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.PlayerId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BoardConverter implements AttributeConverter<Board, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Board board) {
        if (board == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(board.getBoard());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting board to JSON", e);
        }
    }

    @Override
    public Board convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Board.empty();
        }
        try {
            PlayerId[][] grid = objectMapper.readValue(dbData, PlayerId[][].class);
            return new Board(grid);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to board", e);
        }
    }
}