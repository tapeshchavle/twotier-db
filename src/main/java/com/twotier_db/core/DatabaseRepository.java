package com.twotier_db.core;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface — the Strategy contract.
 * <p>
 * Each database adapter (Postgres, MongoDB, Cassandra, etc.) implements
 * this interface for a specific entity type. The {@link DatabaseClientFactory}
 * discovers all implementations via Spring DI.
 *
 * @param <T>  the entity type (must implement {@link DatabaseEntity})
 * @param <ID> the primary key type
 */
public interface DatabaseRepository<T extends DatabaseEntity<ID>, ID> {

    /**
     * Save (create or update) an entity.
     */
    T save(T entity);

    /**
     * Find an entity by its ID.
     */
    Optional<T> findById(ID id);

    /**
     * Retrieve all entities of this type.
     */
    List<T> findAll();

    /**
     * Delete an entity by its ID.
     */
    void deleteById(ID id);

    /**
     * Check if an entity with the given ID exists.
     */
    boolean existsById(ID id);

    /**
     * Count all entities of this type.
     */
    long count();

    /**
     * Self-identify which database this adapter targets.
     */
    DatabaseType getDatabaseType();

    /**
     * The entity class this repository manages — used for type-safe resolution.
     */
    Class<T> getEntityClass();
}
