package com.example.demo.application.handler;

import com.example.demo.application.command.Command;

import io.smallrye.mutiny.Uni;

public interface CommandHandler<C extends Command<R>, R> {
    Uni<R> handle(C command);
}