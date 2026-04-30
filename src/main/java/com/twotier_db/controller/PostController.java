package com.twotier_db.controller;

import com.twotier_db.model.Post;
import com.twotier_db.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Post operations.
 * <p>
 * Posts are stored in <b>MongoDB</b>. The author reference (authorId)
 * points to a User stored in <b>PostgreSQL</b>.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * POST /api/posts — create a new post.
     * Requires authorId (PostgreSQL User ID) in the request body.
     */
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post created = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/posts — list all posts (optionally filtered by author or tag).
     */
    @GetMapping
    public ResponseEntity<List<Post>> getPosts(
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) String tag) {

        List<Post> posts;
        if (authorId != null && !authorId.isBlank()) {
            posts = postService.getPostsByAuthor(authorId);
        } else if (tag != null && !tag.isBlank()) {
            posts = postService.getPostsByTag(tag);
        } else {
            posts = postService.getAllPosts();
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/posts/{id} — get a post by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/posts/{id} — delete a post.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/posts/count?authorId=... — count posts by author.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countPosts(
            @RequestParam String authorId) {
        return ResponseEntity.ok(Map.of("count", postService.countPostsByAuthor(authorId)));
    }
}
