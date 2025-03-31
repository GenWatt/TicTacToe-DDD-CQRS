package com.example.demo.infrastructure.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import jakarta.persistence.Persistence;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/gamedb}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:postgres}")
    private String password;

    @Bean
    public Pool pgPool() {
        String host = extractHostFromJdbcUrl(jdbcUrl);
        int port = extractPortFromJdbcUrl(jdbcUrl);
        String database = extractDatabaseFromJdbcUrl(jdbcUrl);

        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Database: " + database);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(username)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10)
                .setMaxWaitQueueSize(10)
                .setIdleTimeout(300000) // 5 minutes
                .setShared(true);

        return Pool.pool(Vertx.vertx(), connectOptions, poolOptions);
    }

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        try {
            // Skip setting properties - use persistence.xml directly
            return Persistence.createEntityManagerFactory("game-persistence-unit")
                    .unwrap(Mutiny.SessionFactory.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String extractHostFromJdbcUrl(String url) {
        int start = url.indexOf("://") + 3;
        int end = url.indexOf(":", start);
        if (end < 0) {
            end = url.indexOf("/", start);
        }
        return url.substring(start, end);
    }

    private int extractPortFromJdbcUrl(String url) {
        int hostEnd = url.indexOf(":", url.indexOf("://") + 3);
        if (hostEnd < 0) {
            return 5432; // Default PostgreSQL port
        }
        int portEnd = url.indexOf("/", hostEnd);
        return Integer.parseInt(url.substring(hostEnd + 1, portEnd));
    }

    private String extractDatabaseFromJdbcUrl(String url) {
        int dbStart = url.lastIndexOf("/") + 1;
        return url.substring(dbStart);
    }
}