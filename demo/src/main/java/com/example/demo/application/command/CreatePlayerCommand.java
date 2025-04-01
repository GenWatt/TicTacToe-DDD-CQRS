package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.Username;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlayerCommand implements Command<Player> {
    @NotBlank(message = "Username cannot be blank")
    private Username username;
}
