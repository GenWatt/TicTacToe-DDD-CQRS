package com.example.demo.domain.service;

import java.util.List;

import com.example.demo.domain.event.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);
}