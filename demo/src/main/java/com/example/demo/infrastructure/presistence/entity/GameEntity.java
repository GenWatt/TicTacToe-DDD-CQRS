package com.example.demo.infrastructure.presistence.entity;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.GameState;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.presistence.converter.BoardConverter;
import com.example.demo.infrastructure.presistence.converter.MoveListConverter;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {
    @EmbeddedId
    private GameId id;

    @Enumerated(EnumType.STRING)
    private GameState state;

    @Convert(converter = BoardConverter.class)
    @Column(columnDefinition = "TEXT")
    private Board board;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = MoveListConverter.class)
    private List<Move> moves;

    @Column(name = "current_player_move_id")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "current_player_move_id"))
    })
    private PlayerId currentPlayerMoveId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "game_players", joinColumns = @JoinColumn(name = "game_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
    private List<PlayerEntity> players;
}