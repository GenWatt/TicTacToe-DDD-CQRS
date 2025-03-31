package com.example.demo.infrastructure.presistence.converter;

import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Converter
public class MoveListConverter implements AttributeConverter<List<Move>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Move> moves) {
        if (moves == null || moves.isEmpty()) {
            return "[]";
        }

        try {
            // Convert to simplified format
            List<Map<String, Object>> simpleMoves = new ArrayList<>();

            for (Move move : moves) {
                Map<String, Object> simpleMove = Map.of(
                        "x", move.getX(),
                        "y", move.getY(),
                        "playerId", move.getPlayerId().getId().toString());
                simpleMoves.add(simpleMove);
            }

            return objectMapper.writeValueAsString(simpleMoves);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Move list to JSON", e);
        }
    }

    @Override
    public List<Move> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank() || json.equals("null") || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            // Parse the simplified format
            List<Map<String, Object>> simpleMoves = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            List<Move> moves = new ArrayList<>();

            for (Map<String, Object> simpleMove : simpleMoves) {
                int x = (int) simpleMove.get("x");
                int y = (int) simpleMove.get("y");

                // Handle integer or string representation of playerId
                String playerIdStr;
                if (simpleMove.get("playerId") instanceof Integer) {
                    playerIdStr = simpleMove.get("playerId").toString();
                } else {
                    playerIdStr = (String) simpleMove.get("playerId");
                }

                PlayerId playerId = PlayerId.from(UUID.fromString(playerIdStr));
                moves.add(Move.create(x, y, playerId));
            }

            return moves;
        } catch (Exception e) {
            System.err.println("Error parsing moves JSON: " + json);
            e.printStackTrace();
            throw new RuntimeException("Failed to convert JSON to Move list", e);
        }
    }
}