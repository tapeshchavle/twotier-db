package com.twotier_db.mongo.adapter;

import com.twotier_db.core.DatabaseRepository;
import com.twotier_db.core.DatabaseType;
import com.twotier_db.mongo.document.UserDocument;
import com.twotier_db.mongo.repository.UserMongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB adapter for {@link UserDocument}.
 * <p>
 * Implements the {@link DatabaseRepository} strategy by delegating
 * all operations to the Spring Data MongoDB {@link UserMongoRepository}.
 */
@Component
public class MongoUserRepositoryAdapter implements DatabaseRepository<UserDocument, String> {

    private final UserMongoRepository mongoRepository;

    public MongoUserRepositoryAdapter(UserMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public UserDocument save(UserDocument entity) {
        return mongoRepository.save(entity);
    }

    @Override
    public Optional<UserDocument> findById(String id) {
        return mongoRepository.findById(id);
    }

    @Override
    public List<UserDocument> findAll() {
        return mongoRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }

    @Override
    public long count() {
        return mongoRepository.count();
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MONGODB;
    }

    @Override
    public Class<UserDocument> getEntityClass() {
        return UserDocument.class;
    }
}
