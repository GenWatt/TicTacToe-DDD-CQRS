package com.example.demo.infrastructure.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfiguration {

    @Autowired
    private Environment env;

    @Bean
    DataSource flywayDataSource() {
        return DataSourceBuilder.create()
                .url(env.getRequiredProperty("spring.datasource.url"))
                .username(env.getRequiredProperty("spring.datasource.username"))
                .password(env.getRequiredProperty("spring.datasource.password"))
                .driverClassName(env.getRequiredProperty("spring.datasource.driver-class-name"))
                .build();
    }

    @Bean(initMethod = "migrate")
    Flyway flyway(DataSource flywayDataSource) {
        return Flyway.configure()
                .dataSource(flywayDataSource)
                .locations(env.getRequiredProperty("spring.flyway.locations"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }
}
