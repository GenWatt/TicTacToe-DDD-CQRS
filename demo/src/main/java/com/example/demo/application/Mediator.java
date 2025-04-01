package com.example.demo.application;

import com.example.demo.application.command.Command;
import com.example.demo.application.handler.CommandHandler;
import com.example.demo.application.handler.QueryHandler;
import com.example.demo.application.query.Query;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Mediator {

    private final Map<Class<?>, CommandHandler<?, ?>> commandHandlers = new HashMap<>();
    private final Map<Class<?>, QueryHandler<?, ?>> queryHandlers = new HashMap<>();

    public Mediator(ApplicationContext applicationContext) {
        // Register all command handlers
        applicationContext.getBeansOfType(CommandHandler.class).values().forEach(handler -> {
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
            if (generics != null && generics.length == 2) {
                commandHandlers.put(generics[0], handler);
                log.info("Registered command handler: {} for command: {}",
                        handler.getClass().getSimpleName(), generics[0].getSimpleName());
            }
        });

        // Register all query handlers
        applicationContext.getBeansOfType(QueryHandler.class).values().forEach(handler -> {
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), QueryHandler.class);
            if (generics != null && generics.length == 2) {
                queryHandlers.put(generics[0], handler);
                log.info("Registered query handler: {} for query: {}",
                        handler.getClass().getSimpleName(), generics[0].getSimpleName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <R, C extends Command<R>> Uni<R> send(C command) {
        log.info("Processing command: {}", command.getClass().getSimpleName());
        CommandHandler<C, R> handler = (CommandHandler<C, R>) commandHandlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler found for command: " + command.getClass().getName());
        }

        return handler.handle(command);
    }

    @SuppressWarnings("unchecked")
    public <R, Q extends Query<R>> Uni<R> query(Q query) {
        log.info("Processing query: {}", query.getClass().getSimpleName());
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) queryHandlers.get(query.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler found for query: " + query.getClass().getName());
        }

        return handler.handle(query);
    }
}