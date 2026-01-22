# AGENTS.md - KOSP GitHub Harvester

**K-OSP GitHub Harvester** - Spring Batch microservice for collecting GitHub data from KOSP users.

**Tech Stack**: Java 17, Spring Boot 3.5, Spring Batch, Gradle (Kotlin DSL), MySQL, MongoDB, Redis

---

## Build / Test Commands

```bash
./gradlew build                    # Full build with tests
./gradlew build -x test            # Build without tests
./gradlew bootRun                  # Start application (requires .env)

# Test commands
./gradlew test                                         # Run all tests
./gradlew test --tests "CollectionJobTest"             # Single test class
```

**Test Profile**: `@ActiveProfiles("test")` uses H2 in-memory DB, local Redis/MongoDB.

---

## Package Structure

```
io.swkoreatech.kosp.harvester
├── client/          # GitHub API clients (REST, GraphQL)
├── collection/      # Batch step providers
├── config/          # Spring configurations
├── job/             # Batch job definitions
├── launcher/        # Priority job launcher
├── trigger/         # Collection triggers (Redis Stream, Scheduler)
├── user/            # User/GithubUser entities
└── global/          # Shared utilities
```

---

## Strict Coding Rules (MUST FOLLOW)

| Rule | Description |
|------|-------------|
| **Indent Depth <= 1** | No nested control structures. Use early returns. |
| **No `else`/`else if`** | Always use early return pattern. |
| **No Ternary** | `? :` is forbidden. |
| **Method <= 10 lines** | Extract smaller methods if exceeded. |
| **Max 2 Instance Variables** | Except repositories. Use `@Embeddable` for grouping. |
| **No Abbreviations** | `request` not `req`, `response` not `res`. |
| **Max 2 Words** | Keep method/variable names concise. |
| **No Wildcard Imports** | Always use explicit imports. |
| **Version Catalog** | Define deps in `gradle/libs.versions.toml`, use as `libs.xxx`. |

---

## Code Patterns

### Repository (getBy vs findBy)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    
    default User getById(Long id) {
        return findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}
```

### Entity (no @Setter, use business methods)
```java
@Getter @Entity @Table(name = "users")
@NoArgsConstructor(access = PROTECTED) @SuperBuilder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public boolean hasGithubUser() { return githubUser != null; }
}
```

### Step Provider (Spring Batch)
```java
@Component
@RequiredArgsConstructor
public class UserProfileStep implements StepProvider {
    private final GithubRestApiClient apiClient;
    
    @Override
    public String name() { return "userProfileStep"; }
    
    @Override
    public int order() { return 1; }
    
    @Override
    public Step provide(Long userId) { ... }
}
```

---

## Architecture

### Data Flow
```
[Backend] --Redis Stream--> [Harvester]
                                 |
                                 v
                          [PriorityJobLauncher]
                                 |
                                 v
                          [Spring Batch Job]
                                 |
                                 v
                          [MongoDB/MySQL]
```

### Key Components
- **PriorityJobLauncher**: Manages job queue with HIGH/LOW priority, deduplication
- **CollectionTriggerListener**: Consumes Redis Stream messages
- **PeriodicCollectionScheduler**: Triggers collection every N hours
- **StepRegistry**: Collects and orders all StepProvider beans

---

## Environment Variables (`.env`)

DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
Redis: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
MongoDB: `MONGODB_HOST`, `MONGODB_PORT`, `MONGODB_DATABASE`, `MONGODB_USERNAME`, `MONGODB_PASSWORD`, `MONGODB_AUTHENTICATION_DATABASE`

---

## Common Mistakes

- Using `@Setter` on entities (use business methods)
- Nesting if statements (use early returns)
- Adding deps without `libs.versions.toml`
- Using wildcard imports
- Methods longer than 10 lines
