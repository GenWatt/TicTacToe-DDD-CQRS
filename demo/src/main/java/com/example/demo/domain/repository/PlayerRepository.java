package com.example.demo.domain.repository;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.Username;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface PlayerRepository {
    Uni<Player> findById(PlayerId id);

    Uni<Player> findByUsername(Username username);

    Uni<List<Player>> findAllByIds(List<PlayerId> ids);

    Uni<Player> save(Player player);

    Uni<Player> update(Player player);
}