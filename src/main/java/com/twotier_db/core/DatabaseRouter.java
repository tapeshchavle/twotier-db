package com.twotier_db.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * High-level facade that routes database operations to the correct adapter
 * based on application configuration.
 * <p>
 * Supports:
 * <ul>
 *   <li>Default database routing (configurable via {@code app.database.default-type})</li>
 *   <li>Explicit source selection (query a specific database)</li>
 *   <li>Dual-write mode (write to ALL registered adapters for an entity)</li>
 * </ul>
 */
@Component
public class DatabaseRouter {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRouter.class);

    private final DatabaseClientFactory factory;
    private final DatabaseType defaultType;
    private final boolean dualWriteEnabled;

    public DatabaseRouter(
            DatabaseClientFactory factory,
            @Value("${app.database.default-type:POSTGRES}") String defaultType,
            @Value("${app.database.dual-write-enabled:false}") boolean dualWriteEnabled) {
        this.factory = factory;
        this.defaultType = DatabaseType.valueOf(defaultType.toUpperCase());
        this.dualWriteEnabled = dualWriteEnabled;
        log.info("DatabaseRouter initialized — default: {}, dual-write: {}",
                this.defaultType, this.dualWriteEnabled);
    }

    /**
     * Save to the default database (and all others if dual-write is enabled).
     */
    public <T extends DatabaseEntity<ID>, ID> T save(T entity, Class<T> entityClass) {
        T result = getDefaultRepo(entityClass).save(entity);

        if (dualWriteEnabled) {
            dualWrite(entity, entityClass);
        }

        return result;
    }

    /**
     * Save explicitly to a specific database.
     */
    public <T extends DatabaseEntity<ID>, ID> T save(
            T entity, Class<T> entityClass, DatabaseType targetType) {
        return factory.<T, ID>getRepository(targetType, entityClass).save(entity);
    }

    /**
     * Find by ID from the default database.
     */
    public <T extends DatabaseEntity<ID>, ID> Optional<T> findById(
            ID id, Class<T> entityClass) {
        return getDefaultRepo(entityClass).findById(id);
    }

    /**
     * Find by ID from a specific database.
     */
    public <T extends DatabaseEntity<ID>, ID> Optional<T> findById(
            ID id, Class<T> entityClass, DatabaseType sourceType) {
        return factory.<T, ID>getRepository(sourceType, entityClass).findById(id);
    }

    /**
     * Find all from the default database.
     */
    public <T extends DatabaseEntity<ID>, ID> List<T> findAll(Class<T> entityClass) {
        return getDefaultRepo(entityClass).findAll();
    }

    /**
     * Find all from a specific database.
     */
    public <T extends DatabaseEntity<ID>, ID> List<T> findAll(
            Class<T> entityClass, DatabaseType sourceType) {
        return factory.<T, ID>getRepository(sourceType, entityClass).findAll();
    }

    /**
     * Delete by ID from the default database (and all others if dual-write).
     */
    public <T extends DatabaseEntity<ID>, ID> void deleteById(
            ID id, Class<T> entityClass) {
        getDefaultRepo(entityClass).deleteById(id);

        if (dualWriteEnabled) {
            for (DatabaseType type : factory.getRegisteredTypes()) {
                if (type != defaultType && factory.hasRepository(type, entityClass)) {
                    try {
                        factory.<T, ID>getRepository(type, entityClass).deleteById(id);
                    } catch (Exception e) {
                        log.warn("Dual-delete failed on {}: {}", type, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Count from the default database.
     */
    public <T extends DatabaseEntity<ID>, ID> long count(Class<T> entityClass) {
        return getDefaultRepo(entityClass).count();
    }

    /**
     * Get the default repository for an entity class.
     */
    private <T extends DatabaseEntity<ID>, ID> DatabaseRepository<T, ID> getDefaultRepo(
            Class<T> entityClass) {
        return factory.getRepository(defaultType, entityClass);
    }

    /**
     * Write to all non-default databases that have an adapter for this entity.
     */
    private <T extends DatabaseEntity<ID>, ID> void dualWrite(T entity, Class<T> entityClass) {
        for (DatabaseType type : factory.getRegisteredTypes()) {
            if (type != defaultType && factory.hasRepository(type, entityClass)) {
                try {
                    factory.<T, ID>getRepository(type, entityClass).save(entity);
                    log.debug("Dual-write to {} succeeded", type);
                } catch (Exception e) {
                    log.warn("Dual-write to {} failed: {}", type, e.getMessage());
                }
            }
        }
    }

    public DatabaseType getDefaultType() {
        return defaultType;
    }

    public boolean isDualWriteEnabled() {
        return dualWriteEnabled;
    }
}
