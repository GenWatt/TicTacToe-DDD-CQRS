package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.domain.valueObject.Username;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlayerCommand {
    private Username username;
}
