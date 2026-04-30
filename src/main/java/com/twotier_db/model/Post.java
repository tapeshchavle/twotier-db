package com.twotier_db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Domain model for a Post — completely database-agnostic.
 * <p>
 * The service and controller layers work with this POJO.
 * Internally, it is stored as a {@link com.twotier_db.mongo.document.PostDocument}
 * in MongoDB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    private String id;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private List<String> tags;
    private List<String> mediaUrls;
    private long likeCount;
    private long commentCount;
    private Instant createdAt;
    private Instant updatedAt;
}
