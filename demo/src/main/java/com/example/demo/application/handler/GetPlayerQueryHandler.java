package com.example.demo.application.handler;

import com.example.demo.application.query.GetPlayerQuery;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.repository.PlayerRepository;
import com.example.demo.domain.valueObject.PlayerId;
import io.smallrye.mutiny.Uni;
import org.springframework.stereotype.Component;

@Component
public class GetPlayerQueryHandler {
    private final PlayerRepository playerRepository;

    public GetPlayerQueryHandler(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Uni<Player> handle(GetPlayerQuery query) {
        PlayerId playerId = query.getPlayerId();
        return playerRepository.findById(playerId);
    }
}