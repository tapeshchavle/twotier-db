package com.twotier_db.service;

import com.twotier_db.model.User;
import com.twotier_db.postgres.entity.UserEntity;
import com.twotier_db.postgres.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for User operations.
 * <p>
 * Users are stored in <b>PostgreSQL</b> (relational, ACID-compliant).
 * This service directly uses {@link UserJpaRepository} — no routing,
 * no adapters, no abstraction layers. Simple and direct.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserJpaRepository userRepository;

    public UserService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user in PostgreSQL.
     */
    public User createUser(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = userRepository.save(entity);
        log.info("User created in PostgreSQL: {} ({})", saved.getName(), saved.getId());
        return fromEntity(saved);
    }

    /**
     * Find a user by ID from PostgreSQL.
     */
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id).map(this::fromEntity);
    }

    /**
     * Find a user by email from PostgreSQL.
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::fromEntity);
    }

    /**
     * List all users from PostgreSQL.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::fromEntity)
                .toList();
    }

    /**
     * Delete a user by ID from PostgreSQL.
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
        log.info("User deleted from PostgreSQL: {}", id);
    }

    /**
     * Count users in PostgreSQL.
     */
    public long countUsers() {
        return userRepository.count();
    }

    // ── Mapping: Domain ↔ JPA Entity ──

    private UserEntity toEntity(User user) {
        UserEntity entity = UserEntity.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
        entity.setId(user.getId());
        return entity;
    }

    private User fromEntity(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
