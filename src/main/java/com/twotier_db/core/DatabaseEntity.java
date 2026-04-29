package com.twotier_db.core;

/**
 * Marker interface for all database-persisted entities.
 * <p>
 * Every JPA entity, MongoDB document, or any future database entity
 * must implement this interface to participate in the generic
 * repository abstraction.
 *
 * @param <ID> the type of the entity's primary identifier
 */
public interface DatabaseEntity<ID> {

    /**
     * Returns the unique identifier of this entity.
     */
    ID getId();

    /**
     * Sets the unique identifier of this entity.
     */
    void setId(ID id);
}
