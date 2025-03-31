// package com.example.demo.infrastructure.config;

// import org.flywaydb.core.Flyway;
// import org.springframework.beans.factory.InitializingBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.DependsOn;

// @Configuration
// public class FlywayInitializer implements InitializingBean {

// private final Flyway flyway;

// public FlywayInitializer(Flyway flyway) {
// this.flyway = flyway;
// }

// @Override
// public void afterPropertiesSet() {
// // Execute flyway migration on startup, before other beans
// flyway.migrate();
// }

// @Bean
// @DependsOn("flyway")
// public Boolean flywayMigrated() {
// // This bean can be used by other beans that need to ensure
// // Flyway has completed its migration
// return Boolean.TRUE;
// }
// }