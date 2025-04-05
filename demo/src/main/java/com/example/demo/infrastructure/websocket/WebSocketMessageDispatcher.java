package com.example.demo.infrastructure.websocket;

import com.example.demo.infrastructure.websocket.handler.MessageTypeHandler;
import com.example.demo.infrastructure.websocket.handler.WebSocketMessageHandler;
import com.example.demo.infrastructure.websocket.message.MessageType;
import io.smallrye.mutiny.Uni;
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
    private final WebSocketSessionService sessionService;

    public WebSocketMessageDispatcher(ApplicationContext context, WebSocketSessionService sessionService) {
        log.info("Initializing WebSocketMessageDispatcher...");
        this.sessionService = sessionService;
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

    /**
     * Dispatch a message to the appropriate handler reactively
     * 
     * @param session     the WebSocket session
     * @param payload     the message payload
     * @param messageType the type of message
     * @return Uni<Void> that completes when processing is done
     */
    public Uni<Void> dispatch(WebSocketSession session, String payload, MessageType messageType) {
        log.debug("Dispatching message of type {}: {}", messageType, payload);
        WebSocketMessageHandler handler = handlers.get(messageType);

        if (handler != null) {
            // Directly call the handler's handle method which returns a Uni<Void>
            return handler.handle(session, payload)
                    .onFailure().invoke(error -> {
                        log.error("Error processing message type {}: {}", messageType, error.getMessage());
                        sessionService.sendErrorMessage(session,
                                "Server error processing " + messageType + ": " + error.getMessage());
                    });
        } else {
            log.warn("No handler found for message type: {}", messageType);
            return sessionService.sendErrorMessage(session, "Unsupported message type: " + messageType);
        }
    }
}