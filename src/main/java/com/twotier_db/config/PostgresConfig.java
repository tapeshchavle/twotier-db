package com.twotier_db.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * PostgreSQL / JPA configuration.
 * <p>
 * Scopes JPA repository scanning to the postgres module only,
 * preventing conflicts with MongoDB repositories.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.twotier_db.postgres.repository")
@EntityScan(basePackages = "com.twotier_db.postgres.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class PostgresConfig {
    // DataSource, EntityManagerFactory, TransactionManager are all
    // auto-configured by Spring Boot from application.yml properties.
}
