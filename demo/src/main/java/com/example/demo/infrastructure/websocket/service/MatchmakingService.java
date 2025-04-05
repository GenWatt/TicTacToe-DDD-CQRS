package com.example.demo.infrastructure.websocket.service;

import com.example.demo.domain.valueObject.PlayerId;

import io.smallrye.mutiny.Uni;

public interface MatchmakingService {
    public Uni<Boolean> queuePlayer(PlayerId playerId);

    public Uni<Boolean> cancelQueue(PlayerId playerId);

    public Uni<Integer> getQueueSize();
}
