# AGENTS.md - KOSP Backend

**Generated:** 2026-01-23 | **Commit:** ee0d0e8 | **Branch:** fix/fix-everything

**K-OSP (Korea Open Source Platform)** — Spring Boot 3.5 backend for managing open-source contributions by Korean university students.

**Stack**: Java 17, Spring Boot 3.5, Gradle (Kotlin DSL), MySQL, MongoDB, Redis, AWS (S3, SES), JWT

---

## Structure

```
KOSP/
├── backend/           # Main REST API (Spring Web)
├── harvester/         # GitHub data mining (Spring Batch) — see harvester/AGENTS.md
├── common/            # Shared entities (BaseEntity, GithubUser)
├── infra/             # Deployment: Docker, Nginx, DB scripts
├── docs/              # Wiki, onboarding, domain specs
└── references/        # SKKU-OSP legacy reference
```

### Package Layout (backend)
```
io.swkoreatech.kosp
├── domain.{feature}/   # api/, controller/, service/, repository/, model/, dto/
├── global/             # config/, exception/, security/, auth/, init/
└── infra/              # External: github/, email/
```

---

## Commands

```bash
./gradlew build                    # Full build with tests
./gradlew build -x test            # Build without tests
./gradlew bootRun                  # Start backend (requires .env.local)

# Tests (90% Jacoco coverage required)
./gradlew test                                         # All tests
./gradlew test --tests "UserIntegrationTest"           # Single class
./gradlew test --tests "*Integration*"                 # Wildcard
./gradlew test --tests "*.UserServiceTest.signup*"     # Single method

# Harvester
./gradlew :harvester:bootRun       # Start harvester module
```

**Test Profile**: `@ActiveProfiles("test")` → H2 (MySQL mode), Redis DB 1, MongoDB `kosp_test`

---

## Strict Coding Rules (MUST FOLLOW)

| Rule | Violation = Reject |
|------|--------------------|
| **Indent Depth ≤ 1** | No nested if/for. Use early returns. |
| **No `else`/`else if`** | Always early return. |
| **No Ternary `? :`** | Forbidden. |
| **Method ≤ 10 lines** | Extract smaller methods. |
| **Max 2 Instance Vars** | Except repositories. Use `@Embeddable`. |
| **No Abbreviations** | `request` not `req`. |
| **Max 2 Words** | Method/variable names. |
| **No Wildcard Imports** | Explicit imports only. |
| **Version Catalog** | Deps in `gradle/libs.versions.toml` as `libs.xxx`. |
| **No `@Setter`** | Use business methods on entities. |
| **No Hard Delete** | Use `isDeleted` flag (soft delete). |

---

## Code Patterns

### Controller (implements Swagger interface)
```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    
    @Override
    @Permit(permitAll = true, description = "회원가입")
    public ResponseEntity<AuthTokenResponse> signup(
            @RequestBody @Valid UserSignupRequest request,
            @Token SignupToken token) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.signup(request, token));
    }
}
```

### API Interface (Swagger)
```java
@Tag(name = "User", description = "사용자 관리 API")
@RequestMapping("/v1/users")
public interface UserApi {
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    ResponseEntity<AuthTokenResponse> signup(
        @RequestBody @Valid UserSignupRequest request,
        @Parameter(hidden = true) @Token SignupToken token);
}
```

### Repository (getBy vs findBy)
```java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    Optional<User> findById(Long id);  // Returns Optional
    
    default User getById(Long id) {    // Throws if not found
        return findById(id).orElseThrow(
            () -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
```

### DTO (Record + validation)
```java
public record UserSignupRequest(
    @NotBlank(message = "이름은 필수입니다.") String name,
    @Email(message = "이메일 형식이 올바르지 않습니다.") String kutEmail
) {}
```

### Exception (always use enum)
```java
throw new GlobalException(ExceptionMessage.USER_NOT_FOUND);
throw new GlobalException(ExceptionMessage.FORBIDDEN);
```

### Entity (business methods, no @Setter)
```java
@Getter @Entity @Table(name = "users")
@NoArgsConstructor(access = PROTECTED) @SuperBuilder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public void updateInfo(String name) {
        if (name == null) return;
        this.name = name;
    }
    
    public void delete() { this.isDeleted = true; }
}
```

---

## Security

```java
@Permit(permitAll = true)                              // Public
@Permit(permitAll = false)                             // Auth required
@Permit(name = "ADMIN", description = "관리자 전용")   // RBAC permission

// Inject authenticated user
public ResponseEntity<Void> update(@AuthUser User user, Long userId, ...) {
    if (!user.getId().equals(userId)) {
        throw new GlobalException(ExceptionMessage.FORBIDDEN);
    }
}
```

**Flow**: JWT in `Authorization` header → `JwtAuthenticationFilter` → `SecurityContext` → `@AuthUser` resolver

---

## Testing

```java
@SpringBootTest @AutoConfigureMockMvc @Transactional @ActiveProfiles("test")
public abstract class IntegrationTestSupport {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    
    // Helpers: createRole(), createGithubUser(), loginAndGetToken()
}

class UserServiceTest {
    @Nested @DisplayName("signup 메서드")
    class SignupTest {
        @Test @DisplayName("성공")
        void success() { ... }
    }
}
```

**Conventions**:
- Extend `IntegrationTestSupport` for controller tests
- Use `@Nested` + `@DisplayName` (Korean OK) for service tests
- AssertJ for assertions: `assertThat(...).isEqualTo(...)`
- Mock data: `/src/test/resources/data/*.sql`

---

## Module Communication

```
┌─────────────┐     Redis Stream      ┌─────────────┐
│   backend   │ ──────────────────▶   │  harvester  │
│  (REST API) │  github:collection    │ (Batch Job) │
└─────────────┘      :trigger         └─────────────┘
       ▲                                     │
       │         kosp:challenge-check        │
       └─────────────────────────────────────┘
```

- **backend → harvester**: Publishes to `github:collection:trigger` on signup/refresh
- **harvester → backend**: Publishes to `kosp:challenge-check` after score calculation
- **Shared**: MySQL for identity (User, GithubUser), MongoDB for activity data

---

## External Integrations

| Service | Package | Client | Pattern |
|---------|---------|--------|---------|
| GitHub | `infra/github` | `RestClient` (GraphQL) | Sync, encrypted user token |
| Email | `infra/email` | AWS SES SDK v2 | Event-driven, Thymeleaf templates |
| S3 | `domain/upload` | AWS S3 SDK v2 | Presigned URLs for client upload |

---

## Environment Variables (`.env.local`)

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
MONGODB_HOST, MONGODB_PORT, MONGODB_DATABASE, MONGODB_USERNAME, MONGODB_PASSWORD
JWT_SECRET_KEY, JWT_EXPIRATION_ACCESS, JWT_EXPIRATION_REFRESH, JWT_EXPIRATION_SIGNUP
GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET
AWS_ACCESS_KEY, AWS_SECRET_KEY, AWS_REGION, AWS_S3_BUCKET
```

---

## Anti-Patterns (THIS PROJECT)

| ❌ Don't | ✅ Do |
|----------|-------|
| `@Setter` on entities | Business methods: `user.updateInfo(name)` |
| `throw new RuntimeException("msg")` | `throw new GlobalException(ExceptionMessage.X)` |
| Return entity from controller | Return DTO record |
| `JpaRepository<User, Long>` | Custom interface with only needed methods |
| Nested if/else | Early return pattern |
| `implementation("group:artifact:1.0")` | `implementation(libs.xxx)` |
| Directories with " 2" suffix | Clean up sync artifacts |

---

## TODOs in Codebase

- `GithubApiClient:121` — Token validation logic needed
- `UserActivityService:67` — Implement after MongoDB schema rebuild
- `PermissionInitializer:148` — Policy exists check: do NOT overwrite

---

## Notes

- **No CI/CD in repo**: Managed externally
- **Valkey** used instead of Redis (compatible replacement)
- **Package**: `io.swkoreatech.kosp` (not `kr.ac.koreatech.sw.kosp`)
- Admin domain has deep nesting: `domain.admin.{feature}.{layer}`
