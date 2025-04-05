package com.example.demo.infrastructure.websocket;

import com.example.demo.infrastructure.websocket.message.MessageType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebSocketMessageDispatcher messageDispatcher;
    private final WebSocketSessionService sessionService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object playerIdObj = session.getAttributes().get("playerId");

        if (playerIdObj != null) {
            dispatch(session, "Connection established", MessageType.CONNECTION_ESTABLISHED);
        } else {
            log.error("Connection established but no player ID found in session attributes");
            try {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
            } catch (IOException e) {
                log.error("Error closing unauthenticated session", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String payload = status.toString();
        dispatch(session, payload, MessageType.CONNECTION_CLOSED);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        try {
            MessageType messageType = determineMessageType(payload);
            dispatch(session, payload, messageType);
        } catch (Exception e) {
            log.error("Error processing message: {}", payload, e);
            sessionService.sendErrorMessage(session, "Invalid message format: " + e.getMessage());
        }
    }

    private void dispatch(WebSocketSession session, String payload, MessageType messageType) {
        log.info("Dispatching message of type {}: {}", messageType, payload);

        messageDispatcher.dispatch(session, payload, messageType)
                .onFailure().invoke(error -> {
                    log.error("Error dispatching message: {}", error.getMessage());
                    sessionService.sendErrorMessage(session,
                            "Server error processing " + messageType + ": " + error.getMessage());
                })
                .subscribeAsCompletionStage();
    }

    private MessageType determineMessageType(String payload) throws IOException {
        Map<String, Object> jsonMap = objectMapper.readValue(payload,
                new TypeReference<Map<String, Object>>() {
                });

        Object typeObj = jsonMap.get("type");

        if (typeObj == null) {
            throw new IllegalArgumentException("Message type is missing");
        }

        String typeStr = typeObj.toString();

        return MessageType.valueOf(typeStr);
    }
}