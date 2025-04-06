package com.example.demo.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @PostConstruct
    public void init() {

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        System.out.println("Loading environment variables from .env file");
        System.out.println("Environment variables loaded: " + dotenv.toString());
        setEnvIfAbsent(dotenv, "SPRING_DATASOURCE_URL");
        setEnvIfAbsent(dotenv, "SPRING_DATASOURCE_USERNAME");
        setEnvIfAbsent(dotenv, "SPRING_DATASOURCE_PASSWORD");
        setEnvIfAbsent(dotenv, "SECURITY_JWT_SECRET");
    }

    private void setEnvIfAbsent(Dotenv dotenv, String key) {
        if (System.getenv(key) == null && dotenv.get(key) != null) {
            System.setProperty(key.toLowerCase().replace('_', '.'), dotenv.get(key));
        }
    }
}