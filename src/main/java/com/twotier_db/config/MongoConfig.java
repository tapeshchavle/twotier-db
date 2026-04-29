package com.twotier_db.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration.
 * <p>
 * Scopes Mongo repository scanning to the mongo module only,
 * preventing conflicts with JPA repositories.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.twotier_db.mongo.repository")
@EnableMongoAuditing
public class MongoConfig {
    // MongoClient and MongoTemplate are auto-configured by Spring Boot
    // from the spring.data.mongodb.uri property in application.yml.
}
