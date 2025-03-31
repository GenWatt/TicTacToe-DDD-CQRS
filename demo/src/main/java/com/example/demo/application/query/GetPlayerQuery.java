package com.example.demo.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.domain.valueObject.PlayerId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPlayerQuery {
    private PlayerId playerId;
}