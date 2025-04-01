package com.example.demo.domain.service;

import com.example.demo.domain.valueObject.PlayerId;

public interface MatchmakingService {
    public boolean queuePlayer(PlayerId playerId);

    public boolean cancelQueue(PlayerId playerId);

    public int getQueueSize();
}
