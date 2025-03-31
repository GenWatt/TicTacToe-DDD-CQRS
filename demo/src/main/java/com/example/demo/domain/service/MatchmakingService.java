package com.example.demo.domain.service;

import com.example.demo.domain.event.PlayerMatchedEvent;
import com.example.demo.domain.event.PlayerJoinedEvent;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchmakingService {
    private final Queue<PlayerId> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ApplicationEventPublisher eventPublisher;

    public boolean queuePlayer(PlayerId playerId) {
        // Don't add the same player twice
        if (waitingPlayers.contains(playerId)) {
            return false;
        }

        // Check if we can match immediately
        if (!waitingPlayers.isEmpty()) {
            PlayerId opponent = waitingPlayers.poll();
            createMatch(playerId, opponent);
            return true;
        }

        // Otherwise add to queue
        waitingPlayers.add(playerId);
        eventPublisher.publishEvent(new PlayerJoinedEvent(playerId));
        return false;
    }

    public boolean cancelQueue(PlayerId playerId) {
        boolean result = waitingPlayers.remove(playerId);

        if (result) {
            eventPublisher.publishEvent(new PlayerJoinedEvent(playerId));
        }

        return result;
    }

    private void createMatch(PlayerId player1Id, PlayerId player2Id) {
        // Create a new game with these players
        GameId gameId = GameId.create();

        // Publish event that players have been matched
        eventPublisher.publishEvent(new PlayerMatchedEvent(gameId, player1Id, player2Id));
    }

    public int getQueueSize() {
        return waitingPlayers.size();
    }
}