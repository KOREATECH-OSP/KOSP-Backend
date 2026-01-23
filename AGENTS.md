# AGENTS.md - KOSP Backend

**K-OSP (Korea Open Source Platform)** - Spring Boot 3.5 backend for managing open-source contributions by Korean university students.

**Tech Stack**: Java 17, Spring Boot 3.5, Gradle (Kotlin DSL), MySQL, MongoDB, Redis, AWS (S3, SES), JWT

---

## Build / Lint / Test Commands

```bash
./gradlew build                    # Full build with tests
./gradlew build -x test            # Build without tests
./gradlew bootRun                  # Start application (requires .env.local)

# Test commands
./gradlew test                                         # Run all tests
./gradlew test --tests "UserIntegrationTest"           # Single test class
./gradlew test --tests "*Integration*"                 # Wildcard match
./gradlew test --tests "*.UserIntegrationTest.update*" # Single method
```

**Test Profile**: `@ActiveProfiles("test")` uses H2 in-memory DB, local Redis/MongoDB.

---

## Package Structure

```
kr.ac.koreatech.sw.kosp
├── domain.{feature}/       # api/, controller/, service/, repository/, model/, dto/
├── global/                 # config/, exception/, security/, auth/
└── infra/                  # External integrations (GitHub, Email)
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

### Controller (implements Swagger interface)
```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    
    @Override
    @Permit(permitAll = true, description = "회원가입")
    public ResponseEntity<AuthTokenResponse> signup(@RequestBody @Valid UserSignupRequest request, @Token SignupToken token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request, token));
    }
}
```

### API Interface (Swagger annotations)
```java
@Tag(name = "User", description = "사용자 관리 API")
@RequestMapping("/v1/users")
public interface UserApi {
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    ResponseEntity<AuthTokenResponse> signup(@RequestBody @Valid UserSignupRequest request, @Parameter(hidden = true) @Token SignupToken token);
}
```

### Repository (getBy vs findBy)
```java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    Optional<User> findById(Long id);  // Returns Optional
    
    default User getById(Long id) {    // Throws exception if not found
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
```

### DTO (Record with validation)
```java
public record UserSignupRequest(
    @NotBlank(message = "이름은 필수입니다.") String name,
    @Email(message = "이메일 형식이 올바르지 않습니다.") String kutEmail
) {}
```

### Exception Handling
```java
// ExceptionMessage enum defines all errors
throw new GlobalException(ExceptionMessage.USER_NOT_FOUND);
throw new GlobalException(ExceptionMessage.FORBIDDEN);
```

### Entity (no @Setter, use business methods)
```java
@Getter @Entity @Table(name = "users")
@NoArgsConstructor(access = PROTECTED) @SuperBuilder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public void updateInfo(String name) { if (name != null) this.name = name; }  // Business method
    public void delete() { this.isDeleted = true; }  // Soft delete
}
```

---

## Testing

```java
@SpringBootTest @AutoConfigureMockMvc @Transactional @ActiveProfiles("test")
public abstract class IntegrationTestSupport {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    // Helpers: createRole(), createGithubUser(), loginAndGetToken()
}

class UserIntegrationTest extends IntegrationTestSupport {
    @Test @DisplayName("내 정보 수정 성공")
    void update_myInfo_success() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("newName", "intro");
        // when & then
        mockMvc.perform(put("/v1/users/" + userId).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        assertThat(userRepository.findById(userId).get().getName()).isEqualTo("newName");
    }
}
```

---

## Security

```java
@Permit(permitAll = true)                              // Public endpoint
@Permit(permitAll = false)                             // Requires auth
@Permit(name = "ADMIN", description = "관리자 전용")   // RBAC

// Inject authenticated user
public ResponseEntity<Void> update(@AuthUser User user, Long userId, ...) {
    if (!user.getId().equals(userId)) throw new GlobalException(ExceptionMessage.FORBIDDEN);
}
```

---

## Environment Variables (`.env.local`)

DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
Redis: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
MongoDB: `MONGODB_HOST`, `MONGODB_PORT`, `MONGODB_DATABASE`, `MONGODB_USERNAME`, `MONGODB_PASSWORD`
JWT: `JWT_SECRET_KEY`, `JWT_EXPIRATION_ACCESS/REFRESH/SIGNUP`
GitHub: `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
AWS: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION`, `AWS_S3_BUCKET`

---

## Key Patterns

- **API versioning**: All endpoints start with `/v1/`
- **Soft delete**: Use `isDeleted` flag, never hard delete
- **Event-driven**: Spring Events for cross-domain (e.g., `UserSignupEvent`)
- **GitHub integration**: OAuth creates `GithubUser` linked to `User`

---

## Common Mistakes

- Using `@Setter` on entities (use business methods)
- Hardcoding error messages (use `ExceptionMessage` enum)
- Returning entities from controllers (use DTOs)
- Using `JpaRepository` directly (use custom interface with needed methods only)
- Nesting if statements (use early returns)
- Adding deps without `libs.versions.toml`
