package com.example.demo.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class InQueueResponseDto {
    private String message;
}
