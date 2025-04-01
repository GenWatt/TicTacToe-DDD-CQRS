package com.example.demo.infrastructure.presistence.repository;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.repository.GameRepository;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.presistence.entity.GameEntity;
import com.example.demo.infrastructure.presistence.entity.PlayerEntity;
import com.example.demo.infrastructure.presistence.mapper.GameMapper;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final Mutiny.SessionFactory sessionFactory;
    private final GameMapper gameMapper;

    @Override
    public Uni<Game> findById(GameId id) {
        return sessionFactory.withSession(session -> session
                .createQuery("FROM GameEntity g LEFT JOIN FETCH g.players WHERE g.id = :id", GameEntity.class)
                .setParameter("id", id)
                .getSingleResult()
                .onItem().ifNotNull().transform(gameMapper::toDomain));
    }

    @Override
    public Uni<List<Game>> findAll() {
        return sessionFactory.withSession(session -> session.createQuery("FROM GameEntity", GameEntity.class)
                .getResultList()
                .onItem()
                .transform(gameEntities -> gameEntities.stream()
                        .map(gameMapper::toDomain).collect(Collectors.toList())));
    }

    @Override
    public Uni<Game> save(Game game) {
        GameEntity entity = gameMapper.toEntity(game);

        return sessionFactory.withTransaction(session ->
        // First check if the game exists
        session.find(GameEntity.class, game.getId())
                .onItem().transform(existingGame -> existingGame != null)
                .chain(exists -> {
                    return session.createQuery("FROM PlayerEntity p WHERE p.id IN (:ids)", PlayerEntity.class)
                            .setParameter("ids", game.getPlayerIds())
                            .getResultList()
                            .onItem().invoke(playerEntities -> {
                                entity.setPlayers(playerEntities);
                            })
                            .chain(playerEntities -> {
                                if (exists) {
                                    // Update existing game
                                    return session.merge(entity);
                                } else {
                                    // Insert new game
                                    return session.persist(entity);
                                }
                            })
                            .chain(session::flush)
                            .map(v -> game);
                }));
    }

    @Override
    public Uni<Game> findInProgressGameByPlayerId(PlayerId playerId) {
        String hql = "SELECT DISTINCT g " +
                "FROM GameEntity g " +
                "LEFT JOIN FETCH g.players p " +
                "JOIN g.players filterPlayer " +
                "WHERE filterPlayer.id = :playerId AND g.state = :gameState " +
                "ORDER BY g.createdAt DESC";

        return sessionFactory.withSession(session -> session.createQuery(hql, GameEntity.class)
                .setParameter("playerId", playerId)
                .setParameter("gameState", GameState.IN_PROGRESS)
                .setMaxResults(1)
                .getSingleResultOrNull()
                .onItem().ifNotNull().transform(gameMapper::toDomain));
    }
}
