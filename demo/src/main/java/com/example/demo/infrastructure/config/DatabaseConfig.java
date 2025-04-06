package com.example.demo.infrastructure.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class DatabaseConfig {

    private String jdbcUrl = System.getenv("SPRING_DATASOURCE_URL") != null ? System.getenv("SPRING_DATASOURCE_URL")
            : "jdbc:postgresql://localhost:5432/postgres";

    private String username = System.getenv("SPRING_DATASOURCE_USERNAME") != null
            ? System.getenv("SPRING_DATASOURCE_USERNAME")
            : "postgres";

    private String password = System.getenv("SPRING_DATASOURCE_PASSWORD") != null
            ? System.getenv("SPRING_DATASOURCE_PASSWORD")
            : "postgres";

    @Bean
    Pool pgPool() {
        System.out.println("Creating PostgreSQL connection pool with JDBC URL: " + jdbcUrl);
        String host = extractHostFromJdbcUrl(jdbcUrl);
        int port = extractPortFromJdbcUrl(jdbcUrl);
        String database = extractDatabaseFromJdbcUrl(jdbcUrl);
        System.out.println("Creating PostgreSQL connection pool with host: " + host + ", port: " + port + ", database: "
                + database);
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(username)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10)
                .setMaxWaitQueueSize(10)
                .setIdleTimeout(5 * 60 * 1000) // 5 minutes
                .setShared(true);

        return Pool.pool(Vertx.vertx(), connectOptions, poolOptions);
    }

    @Bean
    @DependsOn("flyway")
    Mutiny.SessionFactory sessionFactory() {
        try {
            // Create a map to hold all the properties
            Map<String, Object> properties = new HashMap<>();

            // Add the connection properties
            properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
            properties.put("jakarta.persistence.jdbc.user", username);
            properties.put("jakarta.persistence.jdbc.password", password);

            // For Hibernate Reactive
            properties.put("hibernate.connection.url", jdbcUrl);
            properties.put("hibernate.connection.username", username);
            properties.put("hibernate.connection.password", password);

            System.out.println("Creating Hibernate SessionFactory with URL: " + jdbcUrl);
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
            return 5432;
        }

        int portEnd = url.indexOf("/", hostEnd);
        return Integer.parseInt(url.substring(hostEnd + 1, portEnd));
    }

    private String extractDatabaseFromJdbcUrl(String url) {
        int dbStart = url.lastIndexOf("/") + 1;

        return url.substring(dbStart);
    }
}