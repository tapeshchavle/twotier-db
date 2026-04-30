package com.twotier_db.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a Post stored in the "posts" collection.
 * <p>
 * Posts are stored in MongoDB because they are document-oriented by nature:
 * flexible content, nested media URLs, variable-length tag lists, and
 * denormalized author info for fast reads — all of which suit a NoSQL
 * document store better than rigid relational tables.
 */
@Document(collection = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDocument extends MongoBaseDocument {

    /**
     * The PostgreSQL user ID of the post author.
     * Indexed for fast lookups of "all posts by user X".
     */
    @Indexed
    private String authorId;

    /**
     * Denormalized author name — avoids cross-DB joins.
     */
    private String authorName;

    private String title;

    private String content;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    @Builder.Default
    private long likeCount = 0;

    @Builder.Default
    private long commentCount = 0;
}
