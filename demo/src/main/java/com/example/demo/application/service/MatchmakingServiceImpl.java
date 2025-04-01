package com.example.demo.application.service;

import com.example.demo.domain.event.PlayerMatchedEvent;
import com.example.demo.domain.service.MatchmakingService;
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
public class MatchmakingServiceImpl implements MatchmakingService {
    private final Queue<PlayerId> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ApplicationEventPublisher eventPublisher;

    public boolean queuePlayer(PlayerId playerId) {
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
        GameId gameId = GameId.create();

        // remove players from queue
        waitingPlayers.remove(player1Id);
        waitingPlayers.remove(player2Id);

        eventPublisher.publishEvent(new PlayerMatchedEvent(gameId, player1Id, player2Id));
    }

    public int getQueueSize() {
        return waitingPlayers.size();
    }
}