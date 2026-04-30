package com.twotier_db.mongo.repository;

import com.twotier_db.mongo.document.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for {@link PostDocument}.
 * <p>
 * Spring auto-generates the implementation at runtime.
 */
@Repository
public interface PostMongoRepository extends MongoRepository<PostDocument, String> {

    /**
     * Find all posts by a specific author (PostgreSQL user ID).
     */
    List<PostDocument> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    /**
     * Find all posts containing a specific tag.
     */
    List<PostDocument> findByTagsContaining(String tag);

    /**
     * Count posts by author.
     */
    long countByAuthorId(String authorId);
}
