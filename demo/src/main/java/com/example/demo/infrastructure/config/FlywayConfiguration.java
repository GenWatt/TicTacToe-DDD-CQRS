// package com.example.demo.infrastructure.config;

// import org.flywaydb.core.Flyway;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import javax.sql.DataSource;

// @Configuration
// public class FlywayConfiguration {

// @Value("${spring.datasource.url}")
// private String jdbcUrl;

// @Value("${spring.datasource.username}")
// private String username;

// @Value("${spring.datasource.password}")
// private String password;

// @Value("${spring.datasource.driver-class-name}")
// private String driverClassName;

// @Bean
// public DataSource dataSource() {
// DriverManagerDataSource dataSource = new DriverManagerDataSource();
// dataSource.setDriverClassName(driverClassName);
// dataSource.setUrl(jdbcUrl);
// dataSource.setUsername(username);
// dataSource.setPassword(password);
// return dataSource;
// }

// @Bean(name = "flyway")
// @Primary
// public Flyway flyway(DataSource dataSource) {
// Flyway flyway = Flyway.configure()
// .dataSource(dataSource)
// .baselineOnMigrate(true)
// .locations("classpath:db/migration")
// .load();

// return flyway;
// }
// }