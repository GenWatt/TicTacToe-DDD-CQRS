package com.example.demo.infrastructure.presistence.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.PlayerType;
import com.example.demo.domain.valueObject.Username;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "players")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class PlayerEntity {
    @EmbeddedId
    public PlayerId id;

    @Embedded
    private Username username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "player_type")
    @Enumerated(EnumType.STRING)
    private PlayerType playerType;

    @ManyToMany(mappedBy = "players", fetch = FetchType.LAZY)
    private List<GameEntity> games;
}
