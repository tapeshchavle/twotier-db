package com.twotier_db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Pure domain model — completely database-agnostic.
 * <p>
 * This POJO is what the service and controller layers work with.
 * Mappers convert between this and database-specific entities
 * (JPA {@code UserEntity}, Mongo {@code UserDocument}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private Instant createdAt;
    private Instant updatedAt;
}
