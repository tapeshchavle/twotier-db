package com.twotier_db.model;

import com.twotier_db.mongo.document.UserDocument;
import com.twotier_db.postgres.entity.UserEntity;

/**
 * Mapper utility — converts between the domain {@link User} and
 * database-specific entities ({@link UserEntity}, {@link UserDocument}).
 * <p>
 * Keeps the domain model completely decoupled from any persistence framework.
 */
public final class UserMapper {

    private UserMapper() {
        // utility class
    }

    // ── Domain ↔ JPA (PostgreSQL) ──

    public static User fromEntity(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity entity = UserEntity.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
        entity.setId(user.getId());
        return entity;
    }

    // ── Domain ↔ MongoDB ──

    public static User fromDocument(UserDocument doc) {
        if (doc == null) return null;
        return User.builder()
                .id(doc.getId())
                .name(doc.getName())
                .email(doc.getEmail())
                .phoneNumber(doc.getPhoneNumber())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public static UserDocument toDocument(User user) {
        if (user == null) return null;
        UserDocument doc = UserDocument.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
        doc.setId(user.getId());
        return doc;
    }
}
