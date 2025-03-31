package com.example.demo.domain.aggregate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.demo.domain.event.DomainEvent;

import lombok.Getter;

@Getter
public abstract class AggregateRoot<ID> {
    private ID id;
    private long version;

    private final List<DomainEvent> domainEvents = new CopyOnWriteArrayList<>();

    protected AggregateRoot(ID id, long version) {
        this.id = id;
        this.version = version;
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getAndClearEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void incrementVersion() {
        this.version++;
    }
}
