package com.example.demo.application.dto;

import com.example.demo.domain.aggregate.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private Player player;
    private String token;
}