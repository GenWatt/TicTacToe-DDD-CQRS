package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;

import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.Username;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlayerCommand implements Command<Player> {
    @NotBlank(message = "Username cannot be blank")
    private Username username;

    @NotBlank(message = "Password cannot be blank")
    @Length(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
}
