package com.twotier_db.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract Factory that auto-discovers all {@link DatabaseRepository} beans
 * and provides type-safe resolution by {@link DatabaseType} and entity class.
 * <p>
 * When a new database adapter is added (e.g., Cassandra), it is automatically
 * picked up by Spring DI — <b>no code change required here</b>.
 */
@Component
public class DatabaseClientFactory {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClientFactory.class);

    /**
     * Key: DatabaseType → Value: Map of (EntityClass → Repository)
     */
    private final Map<DatabaseType, Map<Class<?>, DatabaseRepository<?, ?>>> registry =
            new ConcurrentHashMap<>();

    /**
     * Spring injects ALL DatabaseRepository beans automatically.
     */
    public DatabaseClientFactory(List<DatabaseRepository<?, ?>> repositories) {
        for (DatabaseRepository<?, ?> repo : repositories) {
            registry
                    .computeIfAbsent(repo.getDatabaseType(), k -> new ConcurrentHashMap<>())
                    .put(repo.getEntityClass(), repo);
            log.info("Registered adapter: [{}] → entity [{}]",
                    repo.getDatabaseType().getDisplayName(),
                    repo.getEntityClass().getSimpleName());
        }
        log.info("DatabaseClientFactory initialized with {} database type(s): {}",
                registry.size(), registry.keySet());
    }

    /**
     * Resolve a repository for a given database type and entity class.
     *
     * @throws IllegalArgumentException if no adapter is found
     */
    @SuppressWarnings("unchecked")
    public <T extends DatabaseEntity<ID>, ID> DatabaseRepository<T, ID> getRepository(
            DatabaseType type, Class<T> entityClass) {

        Map<Class<?>, DatabaseRepository<?, ?>> typeMap = registry.get(type);
        if (typeMap == null) {
            throw new IllegalArgumentException(
                    "No adapters registered for database type: " + type);
        }

        DatabaseRepository<?, ?> repo = typeMap.get(entityClass);
        if (repo == null) {
            throw new IllegalArgumentException(
                    "No adapter registered for entity [" + entityClass.getSimpleName()
                            + "] on database type [" + type + "]");
        }

        return (DatabaseRepository<T, ID>) repo;
    }

    /**
     * Returns all registered database types.
     */
    public java.util.Set<DatabaseType> getRegisteredTypes() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Check if an adapter exists for a given type + entity.
     */
    public boolean hasRepository(DatabaseType type, Class<?> entityClass) {
        Map<Class<?>, DatabaseRepository<?, ?>> typeMap = registry.get(type);
        return typeMap != null && typeMap.containsKey(entityClass);
    }
}
