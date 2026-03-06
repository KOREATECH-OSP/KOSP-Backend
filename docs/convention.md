# 코드 컨벤션 (Code Convention)

프로젝트 전반에서 준수해야 할 코드 작성 규칙.

---

## 1. Strict Coding Rules

### 1.1 제어문 및 연산자

- **Indent Depth <= 1**: 분기문 중첩 1단계까지만 허용. Guard clause + Early Return 사용.
- **No `else` Keyword**: `else`, `else if` 사용 금지. 항상 조기 리턴.
- **No Ternary Operators**: 삼항 연산자(`? :`) 사용 금지.

### 1.2 클래스 및 메서드

- **Method Length <= 10 Lines**: 길어지면 하위 메서드 또는 다른 클래스로 추출.
- **Small Entities**: 엔티티는 가능한 작게 유지.

### 1.3 데이터 구조 및 변수

- **Collections over Arrays**: 배열 대신 `List`, `Set`, `Map` 등 사용.
- **No Abbreviations**: `req`, `res`, `idx` 금지 → `request`, `response`, `index`.
- **Naming**: 메서드/변수 이름은 최대 2단어 권장.

### 1.4 기타

- **No Wildcard Imports**: `import *` 금지. 명시적 import만 사용.
- **Dependency Injection**: 생성자 주입 (`@RequiredArgsConstructor`).
- **Lombok**: `@Getter`, `@RequiredArgsConstructor`, `@Builder` 위주. `@Setter` 지양 — Entity는 변경 의도가 명확한 비즈니스 메서드 사용.

---

## 2. 아키텍처

### 2.1 패키지 구조

도메인형(Domain-Driven) 구조.

```
io.swkoreatech.kosp
├── domain
│   ├── admin          # 관리자 (배너, 챌린지, 콘텐츠, 멤버, 권한, 정책, 포인트, 역할, 신고, 검색, 연락처)
│   ├── auth           # 인증 (Login, Logout, Token)
│   ├── banner         # 배너
│   ├── challenge      # 챌린지
│   ├── community      # 커뮤니티 (article, board, comment, recruit, team)
│   ├── github         # GitHub 연동
│   ├── mail           # 메일 (이메일 인증)
│   ├── notification   # 알림
│   ├── point          # 포인트
│   ├── report         # 신고
│   ├── search         # 검색
│   ├── upload         # 파일 업로드
│   └── user           # 사용자
├── global
│   ├── auth           # 토큰 저장소, 어노테이션
│   ├── config         # 설정 (WebMvc, Swagger, Security, RabbitMQ)
│   ├── constants      # 상수
│   ├── dto            # 공통 DTO (PageResponse 등)
│   ├── exception      # GlobalException, ExceptionMessage
│   ├── host           # @ServerURL
│   ├── init           # 초기화 (DataInitializer)
│   ├── security       # @AuthUser, @Permit, UserPrincipal
│   └── util           # 유틸리티
└── infra              # 외부 시스템 연동 (SES, GitHub API)
```

### 2.2 도메인 내부 패키지

```
domain/{도메인명}
├── api            # Swagger 인터페이스 (*Api.java)
├── controller     # 구현체 (*Controller.java)
├── service        # 비즈니스 로직
├── model          # Entity
├── repository     # 데이터 접근
└── dto
    ├── request
    └── response
```

### 2.3 Layered Architecture

```
*Api.java (Swagger 명세) → *Controller.java (요청/검증) → *Service.java (비즈니스) → *Repository.java (DB)
```

- **Api Interface**: `@Tag`, `@Operation` 등 Swagger 어노테이션만. Controller가 `implements`.
- **Controller**: `@GetMapping`, `@Valid`, `@Permit` 등 기능 어노테이션. 비즈니스 로직 금지.
- **Service**: `@Transactional` 범위 내 비즈니스 로직.
- **Repository**: `Repository` 인터페이스 상속 (필요한 메서드만 노출). `JpaRepository`는 최소한으로 사용.

### 2.4 Repository 패턴: getBy vs findBy

- `findBy`: `Optional<T>` 반환, null 가능.
- `getBy`: `T` 반환, null이면 `GlobalException` throw. `default` 메서드로 구현.

```java
default User getById(Long id) {
    return findById(id)
        .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
}
```

---

## 3. DTO

- Entity를 API 응답으로 직접 반환 금지. 반드시 Request/Response DTO 사용.
- DTO는 `record` 타입 권장.
- 패키지: `dto/request`, `dto/response`로 구분.

---

## 4. 보안 (@Permit)

메서드 레벨 인가를 `@Permit` 커스텀 어노테이션으로 제어.

```java
@Permit(name = "게시글 작성", description = "게시글을 작성합니다")
@PostMapping
public ArticleResponse create(...) { ... }

@Permit(permitAll = true)  // 인증 없이 접근 가능
@GetMapping
public ArticleResponse get(...) { ... }
```

- `permitAll`: 비인증 접근 허용 여부.
- `name`: RBAC 권한 이름.
- `description`: 권한 설명.

---

## 5. 예외 처리

- `GlobalExceptionHandler` (`@ControllerAdvice`)에서 모든 예외 중앙 처리.
- 비즈니스 예외는 `GlobalException` throw.
- 에러 메시지/상태코드는 `ExceptionMessage` Enum에 정의. 하드코딩 금지.

---

## 6. 응답 포맷

- **성공**: Wrapper 없이 DTO 직접 반환.
- **실패**: `ErrorResponse` (message, status code).

---

## 7. 의존성 관리

`gradle/libs.versions.toml`에서 버전 일괄 관리. `build.gradle.kts`에 직접 버전 명시 금지.

```kotlin
// Bad
implementation("org.springframework.boot:spring-boot-starter-batch")

// Good
implementation(libs.spring.boot.starter.batch)
```
