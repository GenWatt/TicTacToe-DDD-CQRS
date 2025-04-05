package com.example.demo.infrastructure.websocket;

import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider tokenProvider;
    private static final String TOKEN_PARAM = "token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        log.info("Processing WebSocket handshake");

        String token = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            token = httpRequest.getParameter(TOKEN_PARAM);
            log.info("Token from query param: {}", token);

            if (!StringUtils.hasText(token)) {
                String bearerToken = httpRequest.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7);
                }
            }
        }

        if (!StringUtils.hasText(token)) {
            log.warn("Missing token for WebSocket connection");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("X-Auth-Error", "Missing authentication token");
            return false;
        }

        if (!tokenProvider.validateToken(token)) {
            log.warn("Invalid token for WebSocket connection");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("X-Auth-Error", "Invalid authentication token");
            return false;
        }

        try {
            PlayerId playerId = tokenProvider.getPlayerIdFromToken(token);
            attributes.put("playerId", playerId);
            log.info("WebSocket connection authenticated for player: {}", playerId);
        } catch (Exception e) {
            log.error("Error extracting player ID from token", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().add("X-Auth-Error", "Error processing authentication token");
            return false;
        }

        return true; // Allow the handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after handshake
    }
}