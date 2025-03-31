package com.example.demo.infrastructure.presistence.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.repository.PlayerRepository;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.Username;
import com.example.demo.infrastructure.presistence.entity.PlayerEntity;
import com.example.demo.infrastructure.presistence.mapper.PlayerMapper;

import io.smallrye.mutiny.Uni;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {
        private final Mutiny.SessionFactory sessionFactory;
        private final PlayerMapper playerMapper;

        public PlayerRepositoryImpl(Mutiny.SessionFactory sessionFactory, PlayerMapper playerMapper) {
                this.sessionFactory = sessionFactory;
                this.playerMapper = playerMapper;
        }

        @Override
        public Uni<Player> findById(PlayerId id) {
                return sessionFactory.withSession(session -> session.find(PlayerEntity.class, id.getId())
                                .map(entity -> entity != null ? playerMapper.toDomain(entity) : null));
        }

        @Override
        public Uni<List<Player>> findAllByIds(List<PlayerId> ids) {
                return sessionFactory.withSession(
                                session -> session
                                                .createQuery("FROM PlayerEntity p WHERE p.id IN (:ids)",
                                                                PlayerEntity.class)
                                                .setParameter("ids", ids)
                                                .getResultList()
                                                .map(entities -> entities.stream()
                                                                .map(playerMapper::toDomain)
                                                                .collect(Collectors.toList())));
        }

        @Override
        public Uni<Player> save(Player player) {
                PlayerEntity entity = playerMapper.toEntity(player);
                System.out.println("Saving player: " + entity);
                return sessionFactory.withSession(session -> session.persist(entity)
                                .chain(session::flush)
                                .map(v -> player));
        }

        @Override
        public Uni<Player> findByUsername(Username username) {
                return sessionFactory.withSession(
                                session -> session
                                                .createQuery("FROM PlayerEntity p WHERE p.username = :username",
                                                                PlayerEntity.class)
                                                .setParameter("username", username)
                                                .getSingleResultOrNull()
                                                .map(entity -> entity != null ? playerMapper.toDomain(entity) : null));
        }
}
