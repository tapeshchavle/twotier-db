package com.twotier_db.service;

import com.twotier_db.model.Post;
import com.twotier_db.mongo.document.PostDocument;
import com.twotier_db.mongo.repository.PostMongoRepository;
import com.twotier_db.postgres.entity.UserEntity;
import com.twotier_db.postgres.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for Post operations.
 * <p>
 * Posts are stored in <b>MongoDB</b> (document-oriented, flexible schema).
 * The author (User) is stored in <b>PostgreSQL</b> — this service
 * cross-references both databases when needed.
 * <p>
 * This is a real-world example of <b>polyglot persistence</b>:
 * each service directly uses the repository of its target database.
 */
@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostMongoRepository postRepository;
    private final UserJpaRepository userRepository;

    public PostService(PostMongoRepository postRepository, UserJpaRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new post. Validates that the author exists in PostgreSQL,
     * then saves the post in MongoDB.
     */
    public Post createPost(Post post) {
        // Validate author exists in PostgreSQL
        UserEntity author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Author not found in PostgreSQL: " + post.getAuthorId()));

        // Denormalize author name into the post document
        PostDocument doc = toDocument(post);
        doc.setAuthorName(author.getName());

        PostDocument saved = postRepository.save(doc);
        log.info("Post created in MongoDB: {} by author: {} (from PostgreSQL)",
                saved.getId(), author.getName());

        return fromDocument(saved);
    }

    /**
     * Get a post by ID from MongoDB.
     */
    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id).map(this::fromDocument);
    }

    /**
     * Get all posts by a specific author.
     * The authorId is a PostgreSQL User ID — demonstrates cross-DB reference.
     */
    public List<Post> getPostsByAuthor(String authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(this::fromDocument)
                .toList();
    }

    /**
     * Get all posts containing a specific tag.
     */
    public List<Post> getPostsByTag(String tag) {
        return postRepository.findByTagsContaining(tag)
                .stream()
                .map(this::fromDocument)
                .toList();
    }

    /**
     * Get all posts.
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::fromDocument)
                .toList();
    }

    /**
     * Delete a post by ID from MongoDB.
     */
    public void deletePost(String id) {
        postRepository.deleteById(id);
        log.info("Post deleted from MongoDB: {}", id);
    }

    /**
     * Count posts by author.
     */
    public long countPostsByAuthor(String authorId) {
        return postRepository.countByAuthorId(authorId);
    }

    // ── Mapping: Domain ↔ MongoDB Document ──

    private PostDocument toDocument(Post post) {
        return PostDocument.builder()
                .authorId(post.getAuthorId())
                .authorName(post.getAuthorName())
                .title(post.getTitle())
                .content(post.getContent())
                .tags(post.getTags())
                .mediaUrls(post.getMediaUrls())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }

    private Post fromDocument(PostDocument doc) {
        return Post.builder()
                .id(doc.getId())
                .authorId(doc.getAuthorId())
                .authorName(doc.getAuthorName())
                .title(doc.getTitle())
                .content(doc.getContent())
                .tags(doc.getTags())
                .mediaUrls(doc.getMediaUrls())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
