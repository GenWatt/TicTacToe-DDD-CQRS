package com.example.demo.application.service;

import com.example.demo.domain.event.PlayerMatchedEvent;
import com.example.demo.domain.event.PlayerJoinedEvent;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.service.MatchmakingService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchmakingServiceImpl implements MatchmakingService {
    private final Queue<PlayerId> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Uni<Boolean> queuePlayer(PlayerId playerId) {
        return Uni.createFrom().item(() -> {
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
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Override
    public Uni<Boolean> cancelQueue(PlayerId playerId) {
        return Uni.createFrom().item(() -> {
            boolean result = waitingPlayers.remove(playerId);

            if (result) {
                eventPublisher.publishEvent(new PlayerJoinedEvent(playerId));
            }

            return result;
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private void createMatch(PlayerId player1Id, PlayerId player2Id) {
        GameId gameId = GameId.create();

        // remove players from queue
        waitingPlayers.remove(player1Id);
        waitingPlayers.remove(player2Id);

        eventPublisher.publishEvent(new PlayerMatchedEvent(gameId, player1Id, player2Id));
    }

    @Override
    public Uni<Integer> getQueueSize() {
        return Uni.createFrom().item(waitingPlayers::size);
    }
}