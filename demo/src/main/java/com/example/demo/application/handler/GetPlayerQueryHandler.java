package com.example.demo.application.handler;

import com.example.demo.application.query.GetPlayerQuery;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.exception.PlayerNotFoundException;
import com.example.demo.domain.repository.PlayerRepository;
import com.example.demo.domain.valueObject.PlayerId;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPlayerQueryHandler implements QueryHandler<GetPlayerQuery, Player> {
    private final PlayerRepository playerRepository;

    public Uni<Player> handle(GetPlayerQuery query) {
        PlayerId playerId = query.getPlayerId();
        return playerRepository.findById(playerId)
                .onItem().ifNull().failWith(new PlayerNotFoundException())
                .onItem().transform(player -> player);
    }
}