package com.example.demo.infrastructure.websocket.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinMatchmakingMessage.class, name = "JOIN_MATCHMAKING"),
        @JsonSubTypes.Type(value = LeaveMatchmakingMessage.class, name = "LEAVE_MATCHMAKING"),
        @JsonSubTypes.Type(value = PlayMoveMessage.class, name = "PLAY_MOVE"),
// @JsonSubTypes.Type(value = LeaveGameMessage.class, name = "LEAVE_GAME"),
})
@Getter
@NoArgsConstructor
public abstract class MessageBase {
    @JsonProperty("type")
    private MessageType type;

    protected MessageBase(MessageType type) {
        this.type = type;
    }
}
