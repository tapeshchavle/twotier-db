package com.twotier_db.service;

import com.twotier_db.core.DatabaseRouter;
import com.twotier_db.core.DatabaseType;
import com.twotier_db.model.User;
import com.twotier_db.model.UserMapper;
import com.twotier_db.mongo.document.UserDocument;
import com.twotier_db.postgres.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for User operations.
 * <p>
 * Uses {@link DatabaseRouter} for all persistence — completely decoupled
 * from any specific database technology.
 * <p>
 * The service works with the domain {@link User} model and converts
 * to/from database entities via {@link UserMapper}.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DatabaseRouter router;

    public UserService(DatabaseRouter router) {
        this.router = router;
    }

    /**
     * Create a user in the default database (+ dual-write if enabled).
     */
    public User createUser(User user) {
        log.info("Creating user: {} (default DB: {})", user.getEmail(), router.getDefaultType());

        if (router.getDefaultType() == DatabaseType.POSTGRES) {
            UserEntity entity = UserMapper.toEntity(user);
            UserEntity saved = router.save(entity, UserEntity.class);

            // Dual-write to MongoDB if enabled
            if (router.isDualWriteEnabled()) {
                UserDocument doc = UserMapper.toDocument(user);
                doc.setId(saved.getId()); // use same ID for cross-DB consistency
                router.save(doc, UserDocument.class, DatabaseType.MONGODB);
            }

            return UserMapper.fromEntity(saved);
        } else {
            UserDocument doc = UserMapper.toDocument(user);
            UserDocument saved = router.save(doc, UserDocument.class);

            if (router.isDualWriteEnabled()) {
                UserEntity entity = UserMapper.toEntity(user);
                entity.setId(saved.getId());
                router.save(entity, UserEntity.class, DatabaseType.POSTGRES);
            }

            return UserMapper.fromDocument(saved);
        }
    }

    /**
     * Find a user by ID from the default database.
     */
    public Optional<User> getUserById(String id) {
        if (router.getDefaultType() == DatabaseType.POSTGRES) {
            return router.findById(id, UserEntity.class)
                    .map(UserMapper::fromEntity);
        } else {
            return router.findById(id, UserDocument.class)
                    .map(UserMapper::fromDocument);
        }
    }

    /**
     * Find a user by ID from a specific database.
     */
    public Optional<User> getUserById(String id, DatabaseType source) {
        if (source == DatabaseType.POSTGRES) {
            return router.findById(id, UserEntity.class, source)
                    .map(UserMapper::fromEntity);
        } else {
            return router.findById(id, UserDocument.class, source)
                    .map(UserMapper::fromDocument);
        }
    }

    /**
     * List all users from the default database.
     */
    public List<User> getAllUsers() {
        if (router.getDefaultType() == DatabaseType.POSTGRES) {
            return router.findAll(UserEntity.class).stream()
                    .map(UserMapper::fromEntity)
                    .toList();
        } else {
            return router.findAll(UserDocument.class).stream()
                    .map(UserMapper::fromDocument)
                    .toList();
        }
    }

    /**
     * List all users from a specific database.
     */
    public List<User> getAllUsers(DatabaseType source) {
        if (source == DatabaseType.POSTGRES) {
            return router.findAll(UserEntity.class, source).stream()
                    .map(UserMapper::fromEntity)
                    .toList();
        } else {
            return router.findAll(UserDocument.class, source).stream()
                    .map(UserMapper::fromDocument)
                    .toList();
        }
    }

    /**
     * Delete a user by ID from the default database (+ dual-delete if enabled).
     */
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);

        if (router.getDefaultType() == DatabaseType.POSTGRES) {
            router.deleteById(id, UserEntity.class);
            if (router.isDualWriteEnabled()) {
                try {
                    router.deleteById(id, UserDocument.class);
                } catch (Exception e) {
                    log.warn("Dual-delete from MongoDB failed: {}", e.getMessage());
                }
            }
        } else {
            router.deleteById(id, UserDocument.class);
            if (router.isDualWriteEnabled()) {
                try {
                    router.deleteById(id, UserEntity.class);
                } catch (Exception e) {
                    log.warn("Dual-delete from PostgreSQL failed: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Count users in the default database.
     */
    public long countUsers() {
        if (router.getDefaultType() == DatabaseType.POSTGRES) {
            return router.count(UserEntity.class);
        } else {
            return router.count(UserDocument.class);
        }
    }
}
