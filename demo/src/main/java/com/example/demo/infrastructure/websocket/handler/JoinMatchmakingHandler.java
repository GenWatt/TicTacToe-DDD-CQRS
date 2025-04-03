package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.application.service.MatchmakingServiceImpl;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.JoinMatchmakingMessage;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@MessageTypeHandler(MessageType.JOIN_MATCHMAKING)
public class JoinMatchmakingHandler extends AbstractMessageHandler<JoinMatchmakingMessage> {

    private final MatchmakingServiceImpl matchmakingService;
    private final WebSocketSessionService sessionService;

    public JoinMatchmakingHandler(
            ObjectMapper objectMapper,
            MatchmakingServiceImpl matchmakingService,
            WebSocketSessionService sessionService) {
        super(objectMapper, sessionService, JoinMatchmakingMessage.class);

        this.matchmakingService = matchmakingService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleMessage(WebSocketSession session, JoinMatchmakingMessage message) {
        PlayerId playerId = PlayerId.from(message.getPlayerId());

        sessionService.registerPlayer(session, playerId);
        matchmakingService.queuePlayer(playerId);
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_MATCHMAKING;
    }
}
