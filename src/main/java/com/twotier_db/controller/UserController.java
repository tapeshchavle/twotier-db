package com.twotier_db.controller;

import com.twotier_db.core.DatabaseType;
import com.twotier_db.model.User;
import com.twotier_db.service.UserService;
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
 * REST controller for User CRUD operations.
 * <p>
 * Supports optional {@code source} query parameter to target a specific
 * database: {@code ?source=postgres} or {@code ?source=mongodb}.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users — create a new user.
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/users — list all users.
     * Optional: ?source=postgres|mongodb
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false) String source) {

        List<User> users;
        if (source != null && !source.isBlank()) {
            DatabaseType type = resolveDatabaseType(source);
            users = userService.getAllUsers(type);
        } else {
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} — get a user by ID.
     * Optional: ?source=postgres|mongodb
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable String id,
            @RequestParam(required = false) String source) {

        if (source != null && !source.isBlank()) {
            DatabaseType type = resolveDatabaseType(source);
            return userService.getUserById(id, type)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/users/{id} — delete a user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/users/count — count users in the default database.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countUsers() {
        return ResponseEntity.ok(Map.of("count", userService.countUsers()));
    }

    /**
     * Resolve a source string to a {@link DatabaseType}.
     */
    private DatabaseType resolveDatabaseType(String source) {
        return switch (source.toLowerCase()) {
            case "postgres", "postgresql", "pg" -> DatabaseType.POSTGRES;
            case "mongo", "mongodb" -> DatabaseType.MONGODB;
            default -> throw new IllegalArgumentException(
                    "Unknown database source: " + source
                            + ". Supported: postgres, mongodb");
        };
    }
}
