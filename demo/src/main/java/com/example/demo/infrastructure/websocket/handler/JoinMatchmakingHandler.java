package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.application.service.MatchmakingServiceImpl;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.JoinMatchmakingMessage;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
@MessageTypeHandler(MessageType.JOIN_MATCHMAKING)
public class JoinMatchmakingHandler implements WebSocketMessageHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MatchmakingServiceImpl matchmakingService;
    private final WebSocketSessionService sessionService;

    @Override
    public void handle(WebSocketSession session, String payload) throws Exception {
        JoinMatchmakingMessage message = objectMapper.readValue(payload, JoinMatchmakingMessage.class);
        PlayerId playerId = PlayerId.from(message.getPlayerId());
        sessionService.registerPlayer(session, playerId);
        matchmakingService.queuePlayer(playerId);
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_MATCHMAKING;
    }
}
