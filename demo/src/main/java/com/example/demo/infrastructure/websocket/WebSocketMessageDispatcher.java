package com.example.demo.infrastructure.websocket;

import com.example.demo.infrastructure.websocket.handler.MessageTypeHandler;
import com.example.demo.infrastructure.websocket.handler.WebSocketMessageHandler;
import com.example.demo.infrastructure.websocket.message.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WebSocketMessageDispatcher {

    private final Map<MessageType, WebSocketMessageHandler> handlers = new HashMap<>();

    public WebSocketMessageDispatcher(ApplicationContext context) {
        log.info("Initializing WebSocketMessageDispatcher...");
        Map<String, WebSocketMessageHandler> beans = context.getBeansOfType(WebSocketMessageHandler.class);

        for (WebSocketMessageHandler handler : beans.values()) {
            MessageTypeHandler annotation = handler.getClass().getAnnotation(MessageTypeHandler.class);
            if (annotation != null) {

                handlers.put(annotation.value(), handler);
                log.info("Registered WebSocket handler: {} -> {}", annotation.value(),
                        handler.getClass().getSimpleName());
            }
        }
    }

    public void dispatch(WebSocketSession session, String payload, MessageType messageType) {
        log.info("Dispatching message of type {}: {}", messageType, payload);
        WebSocketMessageHandler handler = handlers.get(messageType);

        if (handler != null) {
            handler.handle(session, payload);
        } else {
            log.warn("No handler found for message type: {}", messageType);
        }
    }
}
