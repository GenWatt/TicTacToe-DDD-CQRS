package com.example.demo.infrastructure.websocket;

import com.example.demo.application.service.MatchmakingServiceImpl;
import com.example.demo.domain.service.GameService;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.message.JoinMatchmakingMessage;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.message.PlayMoveMessage;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketMessageDispatcher messageDispatcher;
    private final WebSocketSessionService sessionService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        messageDispatcher.dispatch(session, null, MessageType.CONNECTION_ESTABLISHED);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String payload = status.toString();
        messageDispatcher.dispatch(session, payload, MessageType.CONNECTION_CLOSED);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        try {
            MessageType messageType = determineMessageType(payload);

            messageDispatcher.dispatch(session, payload, messageType);
        } catch (Exception e) {
            log.error("Error processing message", e);
            sessionService.sendErrorMessage(session, "Invalid message format");
        }
    }

    private MessageType determineMessageType(String payload) throws IOException {
        Map<String, Object> jsonMap = objectMapper.readValue(payload, Map.class);
        String typeStr = (String) jsonMap.get("type");
        return MessageType.valueOf(typeStr);
    }
}