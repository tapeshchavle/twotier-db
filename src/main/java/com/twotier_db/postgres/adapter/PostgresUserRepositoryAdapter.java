package com.twotier_db.postgres.adapter;

import com.twotier_db.core.DatabaseRepository;
import com.twotier_db.core.DatabaseType;
import com.twotier_db.postgres.entity.UserEntity;
import com.twotier_db.postgres.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL adapter for {@link UserEntity}.
 * <p>
 * Implements the {@link DatabaseRepository} strategy by delegating
 * all operations to the Spring Data JPA {@link UserJpaRepository}.
 */
@Component
public class PostgresUserRepositoryAdapter implements DatabaseRepository<UserEntity, String> {

    private final UserJpaRepository jpaRepository;

    public PostgresUserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserEntity save(UserEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<UserEntity> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<UserEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRES;
    }

    @Override
    public Class<UserEntity> getEntityClass() {
        return UserEntity.class;
    }
}
