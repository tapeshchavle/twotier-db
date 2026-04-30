<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/MongoDB-7-4DB33D?style=for-the-badge&logo=mongodb&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

# 🏗️ Two-Tier Database — Polyglot Persistence Architecture

> A production-grade **polyglot persistence** architecture where **different entities live in different databases**: Users in PostgreSQL, Posts in MongoDB — extensible to any new database with zero changes to existing code.

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [System Design — Polyglot Persistence](#-system-design--polyglot-persistence)
- [Project Structure](#-project-structure)
- [Request Flow Diagrams](#-request-flow-diagrams)
- [How to Add a New Database](#-how-to-add-a-new-database)
- [API Reference](#-api-reference)
- [Getting Started](#-getting-started)
- [Tech Stack](#-tech-stack)

---

## 🏛️ Architecture Overview

```mermaid
graph TB
    subgraph "🌐 REST API"
        UC["UserController<br>/api/users"]
        PC["PostController<br>/api/posts"]
    end

    subgraph "⚙️ Service Layer"
        US["UserService"]
        PS["PostService"]
    end

    subgraph "🐘 PostgreSQL Tier"
        UJR["UserJpaRepository"]
        UE["UserEntity<br>@Entity @Table"]
        PG[("PostgreSQL<br>users table")]
    end

    subgraph "🍃 MongoDB Tier"
        PMR["PostMongoRepository"]
        PD["PostDocument<br>@Document"]
        MONGO[("MongoDB<br>posts collection")]
    end

    UC --> US
    PC --> PS
    US --> UJR
    PS --> PMR
    PS -.->|"validates author"| UJR
    UJR --> UE --> PG
    PMR --> PD --> MONGO

    style PG fill:#336791,color:#fff
    style MONGO fill:#4DB33D,color:#fff
    style US fill:#2196F3,color:#fff
    style PS fill:#FF9800,color:#fff
```

### Key Design Decision

| Data | Database | Why |
|------|----------|-----|
| **Users** (name, email, phone) | PostgreSQL | Structured, relational, ACID transactions, unique constraints |
| **Posts** (content, tags, media) | MongoDB | Flexible schema, nested arrays, document-oriented, horizontal scaling |

---

## 🎯 System Design — Polyglot Persistence

**Polyglot Persistence** = using the **right database for the right data**. Instead of forcing everything into one database, each entity type lives in the database that best suits its data model.

### Design Principles

```mermaid
graph LR
    subgraph "Principle 1: Entity-Database Binding"
        U["User"] -->|"always"| PG["PostgreSQL"]
        P["Post"] -->|"always"| MG["MongoDB"]
    end

    subgraph "Principle 2: Cross-DB References"
        P2["Post.authorId"] -.->|"references"| U2["User.id"]
    end

    subgraph "Principle 3: Denormalization"
        P3["Post.authorName"] -.->|"copied from"| U3["User.name"]
    end

    style PG fill:#336791,color:#fff
    style MG fill:#4DB33D,color:#fff
```

| Principle | Implementation |
|-----------|---------------|
| **Entity-Database Binding** | Each entity is permanently bound to one database. No routing, no switching. |
| **Cross-DB References** | `Post.authorId` stores the PostgreSQL User ID — a foreign key across databases. |
| **Denormalization** | `Post.authorName` is copied from User to avoid cross-DB joins at read time. |
| **Service Orchestration** | `PostService` uses both `PostMongoRepository` AND `UserJpaRepository` to validate authors. |
| **Open/Closed Principle** | Adding a new entity+database = new files only. Zero changes to existing code. |

### Class Diagram

```mermaid
classDiagram
    class PostgresBaseEntity {
        <<abstract>>
        -id: String (UUID)
        -createdAt: Instant
        -updatedAt: Instant
    }

    class UserEntity {
        -name: String
        -email: String
        -phoneNumber: String
    }

    class UserJpaRepository {
        <<interface>>
        +findByEmail(email) Optional
        +existsByEmail(email) boolean
    }

    class UserService {
        -userRepository: UserJpaRepository
        +createUser(User) User
        +getUserById(id) Optional
        +getAllUsers() List
        +deleteUser(id)
    }

    class UserController {
        -userService: UserService
        +POST /api/users
        +GET /api/users
        +GET /api/users/id
        +DELETE /api/users/id
    }

    class MongoBaseDocument {
        <<abstract>>
        -id: String
        -createdAt: Instant
        -updatedAt: Instant
    }

    class PostDocument {
        -authorId: String
        -authorName: String
        -title: String
        -content: String
        -tags: List
        -mediaUrls: List
        -likeCount: long
        -commentCount: long
    }

    class PostMongoRepository {
        <<interface>>
        +findByAuthorId(id) List
        +findByTagsContaining(tag) List
        +countByAuthorId(id) long
    }

    class PostService {
        -postRepository: PostMongoRepository
        -userRepository: UserJpaRepository
        +createPost(Post) Post
        +getPostsByAuthor(id) List
        +getPostsByTag(tag) List
        +deletePost(id)
    }

    class PostController {
        -postService: PostService
        +POST /api/posts
        +GET /api/posts
        +GET /api/posts/id
        +DELETE /api/posts/id
    }

    PostgresBaseEntity <|-- UserEntity
    MongoBaseDocument <|-- PostDocument
    UserController --> UserService
    UserService --> UserJpaRepository
    UserJpaRepository --> UserEntity
    PostController --> PostService
    PostService --> PostMongoRepository
    PostService --> UserJpaRepository
    PostMongoRepository --> PostDocument
```

---

## 📁 Project Structure

```
two-tier-db/
├── docker-compose.yml                                 # PostgreSQL + MongoDB
├── pom.xml                                            # Maven dependencies
├── src/main/
│   ├── java/com/twotier_db/
│   │   ├── DemoApplication.java                       # Entry point
│   │   │
│   │   ├── config/                                    # ⚙️ DATABASE CONFIGS
│   │   │   ├── PostgresConfig.java                    #   JPA + auditing
│   │   │   └── MongoConfig.java                       #   MongoDB + auditing
│   │   │
│   │   ├── postgres/                                  # 🐘 POSTGRESQL MODULE
│   │   │   ├── entity/
│   │   │   │   ├── PostgresBaseEntity.java            #   Base: id + timestamps
│   │   │   │   └── UserEntity.java                    #   @Entity → users table
│   │   │   └── repository/
│   │   │       └── UserJpaRepository.java             #   Spring Data JPA
│   │   │
│   │   ├── mongo/                                     # 🍃 MONGODB MODULE
│   │   │   ├── document/
│   │   │   │   ├── MongoBaseDocument.java             #   Base: id + timestamps
│   │   │   │   └── PostDocument.java                  #   @Document → posts collection
│   │   │   └── repository/
│   │   │       └── PostMongoRepository.java           #   Spring Data MongoDB
│   │   │
│   │   ├── model/                                     # 📦 DOMAIN MODELS
│   │   │   ├── User.java                              #   DB-agnostic User POJO
│   │   │   └── Post.java                              #   DB-agnostic Post POJO
│   │   │
│   │   ├── service/                                   # 🔧 BUSINESS LOGIC
│   │   │   ├── UserService.java                       #   Users → PostgreSQL
│   │   │   └── PostService.java                       #   Posts → MongoDB
│   │   │
│   │   └── controller/                                # 🌐 REST API
│   │       ├── UserController.java                    #   /api/users
│   │       └── PostController.java                    #   /api/posts
│   │
│   └── resources/
│       └── application.yml
└── README.md
```

---

## 🔄 Request Flow Diagrams

### Create User → PostgreSQL

```mermaid
sequenceDiagram
    participant Client
    participant UserController
    participant UserService
    participant UserJpaRepository
    participant PostgreSQL

    Client->>UserController: POST /api/users {name, email}
    UserController->>UserService: createUser(User)
    UserService->>UserService: toEntity(User) → UserEntity
    UserService->>UserJpaRepository: save(UserEntity)
    UserJpaRepository->>PostgreSQL: INSERT INTO users (id, name, email, ...) VALUES (...)
    PostgreSQL-->>UserJpaRepository: saved row with UUID
    UserJpaRepository-->>UserService: UserEntity
    UserService->>UserService: fromEntity(UserEntity) → User
    UserService-->>UserController: User
    UserController-->>Client: 201 Created {id, name, email, createdAt}
```

### Create Post → MongoDB (with PostgreSQL validation)

```mermaid
sequenceDiagram
    participant Client
    participant PostController
    participant PostService
    participant UserJpaRepository
    participant PostgreSQL
    participant PostMongoRepository
    participant MongoDB

    Client->>PostController: POST /api/posts {authorId, title, content, tags}
    PostController->>PostService: createPost(Post)

    Note over PostService: Cross-DB validation!
    PostService->>UserJpaRepository: findById(authorId)
    UserJpaRepository->>PostgreSQL: SELECT * FROM users WHERE id = ?
    PostgreSQL-->>UserJpaRepository: UserEntity {name: "Tapesh"}
    UserJpaRepository-->>PostService: UserEntity

    Note over PostService: Denormalize author name
    PostService->>PostService: doc.setAuthorName("Tapesh")

    PostService->>PostMongoRepository: save(PostDocument)
    PostMongoRepository->>MongoDB: db.posts.insertOne({authorId, authorName, title, ...})
    MongoDB-->>PostMongoRepository: saved document
    PostMongoRepository-->>PostService: PostDocument
    PostService->>PostService: fromDocument(PostDocument) → Post
    PostService-->>PostController: Post
    PostController-->>Client: 201 Created {id, authorId, authorName, title, ...}
```

### Get Posts by Author

```mermaid
sequenceDiagram
    participant Client
    participant PostController
    participant PostService
    participant PostMongoRepository
    participant MongoDB

    Client->>PostController: GET /api/posts?authorId=abc-123
    PostController->>PostService: getPostsByAuthor("abc-123")
    PostService->>PostMongoRepository: findByAuthorIdOrderByCreatedAtDesc("abc-123")
    PostMongoRepository->>MongoDB: db.posts.find({authorId: "abc-123"}).sort({createdAt: -1})
    MongoDB-->>PostMongoRepository: [PostDocument, PostDocument, ...]
    PostMongoRepository-->>PostService: List of PostDocuments
    PostService->>PostService: map each → Post domain objects
    PostService-->>PostController: List of Posts
    PostController-->>Client: 200 OK [{...}, {...}]
```

---

## 🔌 How to Add a New Database

Adding a new database (e.g., **Redis** for sessions) requires only **new files** — zero changes to existing code.

```mermaid
flowchart TD
    A["1️⃣ Add dependency to pom.xml<br>spring-boot-starter-data-redis"] --> B
    B["2️⃣ Create config<br>config/RedisConfig.java"] --> C
    C["3️⃣ Create entity<br>redis/entity/SessionEntity.java"] --> D
    D["4️⃣ Create repository<br>redis/repository/SessionRedisRepository.java"] --> E
    E["5️⃣ Create service<br>service/SessionService.java"] --> F
    F["6️⃣ Create controller<br>controller/SessionController.java"] --> G
    G["✅ Done! No existing files changed"]

    style A fill:#E3F2FD,stroke:#1976D2
    style G fill:#4CAF50,color:#fff
```

### Example: Adding Cassandra for Analytics

```
cassandra/
├── entity/
│   ├── CassandraBaseEntity.java
│   └── AnalyticsEventEntity.java       # @Table
├── repository/
│   └── AnalyticsEventRepository.java   # CassandraRepository<...>
```

```java
// service/AnalyticsService.java — directly uses CassandraRepository
@Service
public class AnalyticsService {
    private final AnalyticsEventRepository analyticsRepo;
    // ... no routing, no factory, just direct usage
}
```

**Files changed in existing code: 0**

---

## 📡 API Reference

### Users (PostgreSQL)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users` | Create a user |
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `DELETE` | `/api/users/{id}` | Delete a user |
| `GET` | `/api/users/count` | Count users |

### Posts (MongoDB)

| Method | Endpoint | Query Params | Description |
|--------|----------|-------------|-------------|
| `POST` | `/api/posts` | — | Create a post (requires `authorId`) |
| `GET` | `/api/posts` | `?authorId=...` or `?tag=...` | List posts (filterable) |
| `GET` | `/api/posts/{id}` | — | Get post by ID |
| `DELETE` | `/api/posts/{id}` | — | Delete a post |
| `GET` | `/api/posts/count` | `?authorId=...` | Count posts by author |

### Usage Examples

```bash
# 1. Create a user (→ PostgreSQL)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Tapesh","email":"tapesh@example.com","phoneNumber":"+91-9999999999"}'

# Response: {"id":"abc-123", "name":"Tapesh", "email":"tapesh@example.com", ...}

# 2. Create a post by that user (→ MongoDB)
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "authorId": "abc-123",
    "title": "My First Post",
    "content": "Hello from MongoDB!",
    "tags": ["intro", "hello"],
    "mediaUrls": ["https://example.com/photo.jpg"]
  }'

# Response: {"id":"...", "authorId":"abc-123", "authorName":"Tapesh", "title":"My First Post", ...}

# 3. Get all posts by author
curl http://localhost:8080/api/posts?authorId=abc-123

# 4. Get all posts with a tag
curl http://localhost:8080/api/posts?tag=intro
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Run

```bash
git clone https://github.com/tapeshchavle/multiple_db.git
cd multiple_db

# Start databases
docker-compose up -d

# Run the application
./mvnw spring-boot:run
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Spring Boot 4.0.6** | Application framework |
| **Spring Data JPA** | PostgreSQL ORM (Users) |
| **Spring Data MongoDB** | MongoDB ODM (Posts) |
| **PostgreSQL 16** | Relational database for Users |
| **MongoDB 7** | Document database for Posts |
| **Lombok** | Boilerplate reduction |
| **Docker Compose** | Local database orchestration |

---

<p align="center">
  <b>Built with ❤️ by <a href="https://github.com/tapeshchavle">Tapesh Chavle</a></b>
</p>
