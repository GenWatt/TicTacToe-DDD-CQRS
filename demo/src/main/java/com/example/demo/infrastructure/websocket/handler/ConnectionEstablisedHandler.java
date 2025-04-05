package com.example.demo.infrastructure.websocket.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@MessageTypeHandler(MessageType.CONNECTION_ESTABLISHED)
@RequiredArgsConstructor
public class ConnectionEstablisedHandler implements WebSocketMessageHandler {

    private final WebSocketSessionService sessionService;

    @Override
    public Uni<Void> handle(WebSocketSession session, String payload) {
        return sessionService.registerSession(session)
                .onItem().invoke(() -> log.info("WebSocket connection established: {}", session.getId()))
                .replaceWithVoid();
    }

    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_ESTABLISHED;
    }
}
