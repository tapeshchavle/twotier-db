package com.twotier_db.core;

/**
 * Enum representing supported database types.
 * <p>
 * To add a new database, simply add a new constant here and create
 * the corresponding adapter module — zero changes to existing code.
 */
public enum DatabaseType {

    POSTGRES("PostgreSQL", Category.SQL),
    MONGODB("MongoDB", Category.NOSQL);

    private final String displayName;
    private final Category category;

    DatabaseType(String displayName, Category category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isSql() {
        return category == Category.SQL;
    }

    public boolean isNoSql() {
        return category == Category.NOSQL;
    }

    /**
     * Database category — helps with category-level routing decisions.
     */
    public enum Category {
        SQL,
        NOSQL
    }
}
