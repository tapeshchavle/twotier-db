package com.twotier_db.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Base class for all MongoDB documents.
 * <p>
 * Provides a String ID and audit timestamps.
 * All MongoDB documents should extend this class.
 */
@Getter
@Setter
public abstract class MongoBaseDocument {

    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
