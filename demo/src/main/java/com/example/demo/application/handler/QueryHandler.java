package com.example.demo.application.handler;

import com.example.demo.application.query.Query;

import io.smallrye.mutiny.Uni;

public interface QueryHandler<Q extends Query<R>, R> {
    Uni<R> handle(Q query);
}
