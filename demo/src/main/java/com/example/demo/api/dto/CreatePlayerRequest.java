package com.example.demo.api.dto;

import com.example.demo.domain.valueObject.Username;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlayerRequest {

    private Username username;
}
