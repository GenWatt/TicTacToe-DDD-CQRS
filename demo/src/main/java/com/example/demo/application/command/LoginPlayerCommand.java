package com.example.demo.application.command;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.Username;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginPlayerCommand implements Command<Player> {
    private Username username;
}
