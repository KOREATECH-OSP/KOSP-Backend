# GitHub Organization 등록 기능 구현 계획

> 작성일: 2026-06-23
> 목표: GitHub OAuth 확장으로 Organization Owner가 조직을 K-OSP에 등록하고, 멤버·저장소를 자동 매핑

---

## Claude 행동 요령

> 이 섹션은 Claude가 이 문서를 기반으로 작업할 때 따르는 규칙이다.

### 트리거
- 사용자가 `step1`, `step2`, ... `step8` 을 입력하면 해당 Step을 구현한다.

### 구현 절차 (매 Step 공통)

```
1. 이 문서에서 해당 Step 내용을 읽는다.
2. 관련된 기존 코드를 먼저 읽고 파악한다. (convention.md 준수 확인)
3. 코드를 작성한다.
4. 작성 완료 후 스스로 재검토한다:
   - 컨벤션 위반 여부 (else 금지, indent depth, 메서드 10줄 이하 등)
   - 누락된 파일 없는지
   - 연관 파일(ExceptionMessage, build.gradle 등) 수정 빠진 것 없는지
   - 컴파일 오류 가능성 있는 코드 없는지
5. 개발일지 항목을 이 문서 하단 [개발일지] 섹션에 추가한다.
6. 개발일지 작성 완료 후 커밋&푸시한다:
   - git add . (해당 Step 파일만)
   - git commit -m "조직등록 stepN"
   - git push origin develop
```

### 개발일지 작성 형식

```markdown
### Step N — 제목 (YYYY-MM-DD)
- 구현한 내용 요약 (파일명 포함)
- 특이사항 또는 결정한 내용
- 다음 Step과의 연결 포인트
```

---

---

## 전체 구현 순서

```
Step 1. Flyway 마이그레이션 (V17~V19) — DB 스키마
Step 2. common 모듈 — Organization 엔티티·레포지토리
Step 3. backend infra — GitHub Org API 클라이언트
Step 4. backend domain — Organization 서비스·컨트롤러·DTO
Step 5. backend — OAuth2UserService 자동 매핑 훅
Step 6. backend — OAuth scope 추가 및 재인증 처리
Step 7. backend — 관리자 API
Step 8. 기능명세서.md 업데이트
```

---

## Step 1. Flyway 마이그레이션

**파일 위치**: `Backend/flyway/src/main/resources/db/migration/`

### V17__create_organizations.sql

```sql
CREATE TABLE organizations (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    github_org_id         BIGINT       NOT NULL UNIQUE,
    github_org_name       VARCHAR(255) NOT NULL,
    display_name          VARCHAR(255),
    avatar_url            VARCHAR(500),
    registered_by_user_id BIGINT       NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    CONSTRAINT fk_org_registered_by FOREIGN KEY (registered_by_user_id) REFERENCES users(id)
);
```

### V18__create_organization_members.sql

```sql
CREATE TABLE organization_members (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id  BIGINT       NOT NULL,
    user_id          BIGINT,
    github_user_id   BIGINT       NOT NULL,
    github_username  VARCHAR(255) NOT NULL,
    role             VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'NOT_JOINED',
    joined_at        TIMESTAMP,
    synced_at        TIMESTAMP    NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_org_member_org  FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_org_member_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_org_member      UNIQUE (organization_id, github_user_id)
);
```

### V19__create_organization_repositories.sql

```sql
CREATE TABLE organization_repositories (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id      BIGINT       NOT NULL,
    github_repo_id       BIGINT       NOT NULL UNIQUE,
    repository_name      VARCHAR(255) NOT NULL,
    repository_full_name VARCHAR(255) NOT NULL,
    repository_url       VARCHAR(500) NOT NULL,
    visibility           VARCHAR(20)  NOT NULL,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    synced_at            TIMESTAMP    NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_org_repo_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

---

## Step 2. common 모듈 — 엔티티 및 레포지토리

**패키지**: `io.swkoreatech.kosp.common.organization`

### 2-1. Enum 타입 3개

**파일**: `common/.../organization/model/OrganizationStatus.java`
```java
public enum OrganizationStatus {
    ACTIVE, PENDING, DISCONNECTED
}
```

**파일**: `common/.../organization/model/OrganizationMemberRole.java`
```java
public enum OrganizationMemberRole {
    OWNER, MEMBER
}
```

**파일**: `common/.../organization/model/OrganizationMemberStatus.java`
```java
public enum OrganizationMemberStatus {
    LINKED, NOT_JOINED, REMOVED
}
```

### 2-2. Organization 엔티티

**파일**: `common/.../organization/model/Organization.java`

- `extends BaseEntity` (createdAt, updatedAt)
- 필드: `id`, `githubOrgId`, `githubOrgName`, `displayName`, `avatarUrl`, `registeredByUserId`, `status(OrganizationStatus)`
- `@OneToMany(mappedBy = "organization")` → `List<OrganizationMember> members`
- `@OneToMany(mappedBy = "organization")` → `List<OrganizationRepository> repositories`
- 비즈니스 메서드: `disconnect()`, `activate()`

### 2-3. OrganizationMember 엔티티

**파일**: `common/.../organization/model/OrganizationMember.java`

- `extends BaseEntity`
- 필드: `id`, `organization(ManyToOne)`, `userId(nullable)`, `githubUserId`, `githubUsername`, `role(OrganizationMemberRole)`, `status(OrganizationMemberStatus)`, `joinedAt`, `syncedAt`
- 비즈니스 메서드: `link(Long userId)`, `remove()`

### 2-4. OrganizationRepository 엔티티

**파일**: `common/.../organization/model/OrganizationRepository.java`

- `extends BaseEntity`
- 필드: `id`, `organization(ManyToOne)`, `githubRepoId`, `repositoryName`, `repositoryFullName`, `repositoryUrl`, `visibility`, `isActive`, `syncedAt`
- 비즈니스 메서드: `deactivate()`

### 2-5. 레포지토리 인터페이스

**파일**: `common/.../organization/repository/OrganizationJpaRepository.java`
```java
Optional<Organization> findByGithubOrgId(Long githubOrgId);
List<Organization> findAllByRegisteredByUserId(Long userId);
```

**파일**: `common/.../organization/repository/OrganizationRepository.java`
```java
// getBy 패턴
default Organization getByGithubOrgId(Long githubOrgId) {
    return findByGithubOrgId(githubOrgId)
        .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND));
}
```

**파일**: `common/.../organization/repository/OrganizationMemberJpaRepository.java`
```java
List<OrganizationMember> findAllByOrganizationId(Long organizationId);
Optional<OrganizationMember> findByOrganizationIdAndGithubUserId(Long organizationId, Long githubUserId);
List<OrganizationMember> findAllByGithubUserId(Long githubUserId);  // 로그인 시 자동 매핑용
```

**파일**: `common/.../organization/repository/OrganizationRepositoryJpaRepository.java`
```java
List<OrganizationRepository> findAllByOrganizationId(Long organizationId);
```

### 2-6. ExceptionMessage 추가

기존 `ExceptionMessage` Enum에 추가:
```
ORGANIZATION_NOT_FOUND
ORGANIZATION_ALREADY_REGISTERED
ORGANIZATION_OWNER_REQUIRED
ORGANIZATION_MEMBER_NOT_FOUND
GITHUB_REAUTH_REQUIRED
```

---

## Step 3. backend infra — GitHub Org API 클라이언트

**파일**: `backend/.../infra/github/GithubOrgApiClient.java`

WebFlux `WebClient` 기반 가벼운 클라이언트. harvester와 무관하게 독립 작성.

### 호출할 GitHub REST API 3개

| 메서드 | GitHub API | 설명 |
|--------|-----------|------|
| `getMyOrgMemberships(token)` | `GET /user/memberships/orgs` | 내가 속한 조직 멤버십 목록 (role 포함) |
| `getOrgMembers(token, orgName)` | `GET /orgs/{org}/members` | 조직 멤버 목록 |
| `getOrgRepos(token, orgName)` | `GET /orgs/{org}/repos` | 조직 저장소 목록 |

### 응답 record DTO (infra 내부용)

**파일**: `backend/.../infra/github/dto/GithubOrgMembership.java`
```java
public record GithubOrgMembership(
    String role,                   // "admin" | "member"
    String state,                  // "active" | "pending"
    GithubOrgSummary organization
) {}
```

**파일**: `backend/.../infra/github/dto/GithubOrgSummary.java`
```java
public record GithubOrgSummary(
    Long id,
    String login,
    String avatarUrl
) {}
```

**파일**: `backend/.../infra/github/dto/GithubOrgMember.java`
```java
public record GithubOrgMember(
    Long id,
    String login,
    String avatarUrl
) {}
```

**파일**: `backend/.../infra/github/dto/GithubOrgRepo.java`
```java
public record GithubOrgRepo(
    Long id,
    String name,
    String fullName,
    String htmlUrl,
    String visibility   // "public" | "private"
) {}
```

### WebClient 설정 사항

- baseUrl: `https://api.github.com`
- 헤더: `Authorization: Bearer {token}`, `Accept: application/vnd.github+json`
- 페이지네이션: `per_page=100` (MVP는 첫 페이지만)
- 403/429 응답 → `GlobalException(GITHUB_REAUTH_REQUIRED)` throw

---

## Step 4. backend domain — Organization 도메인

**패키지**: `io.swkoreatech.kosp.domain.organization`

### 4-1. API 인터페이스

**파일**: `backend/.../domain/organization/api/OrganizationApi.java`

Swagger `@Tag`, `@Operation` 어노테이션만 정의.

### 4-2. Controller

**파일**: `backend/.../domain/organization/controller/OrganizationController.java`

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| `GET` | `/v1/organizations/available` | 내가 Owner인 조직 목록 | 인증 필요 |
| `POST` | `/v1/organizations` | 조직 등록 | 인증 필요 |
| `GET` | `/v1/organizations/my` | 내가 등록한 조직 목록 | 인증 필요 |
| `GET` | `/v1/organizations/{organizationId}` | 조직 상세 | 인증 필요 |

### 4-3. Service

**파일**: `backend/.../domain/organization/service/OrganizationService.java`

**핵심 메서드:**

```
getAvailableOrganizations(User user)
  → GithubUser.githubToken 복호화
  → GithubOrgApiClient.getMyOrgMemberships(token)
  → role == "admin" 인 것만 필터
  → 이미 등록된 조직 여부 포함하여 반환

registerOrganization(User user, Long githubOrgId)
  → 이미 등록된 조직면 → GlobalException(ORGANIZATION_ALREADY_REGISTERED)
  → Organization 저장
  → syncMembers(organization, token)      [private]
  → syncRepositories(organization, token) [private]

syncMembers(Organization organization, String token)
  → GithubOrgApiClient.getOrgMembers(token, orgName)
  → UserRepository.findAllByGithubIds(githubUserIds) 로 기존 사용자 조회
  → 매핑되면 LINKED / 아니면 NOT_JOINED
  → OrganizationMember 일괄 저장

syncRepositories(Organization organization, String token)
  → GithubOrgApiClient.getOrgRepos(token, orgName)
  → OrganizationRepository 일괄 저장
```

### 4-4. DTO

**Request:**

`dto/request/OrganizationRegisterRequest.java`
```java
public record OrganizationRegisterRequest(
    @NotNull Long githubOrgId
) {}
```

**Response:**

`dto/response/AvailableOrganizationResponse.java`
```java
public record AvailableOrganizationResponse(
    Long githubOrgId,
    String githubOrgName,
    String avatarUrl,
    boolean alreadyRegistered
) {}
```

`dto/response/OrganizationResponse.java`
```java
public record OrganizationResponse(
    Long id,
    Long githubOrgId,
    String githubOrgName,
    String displayName,
    String avatarUrl,
    String status,
    LocalDateTime createdAt
) {}
```

`dto/response/OrganizationDetailResponse.java`
```java
public record OrganizationDetailResponse(
    Long id,
    String githubOrgName,
    String displayName,
    String avatarUrl,
    String status,
    int totalMemberCount,
    int linkedMemberCount,
    int repositoryCount,
    LocalDateTime createdAt
) {}
```

---

## Step 5. OAuth2UserService — 로그인 시 자동 매핑 훅

**수정 파일**: `backend/.../domain/auth/oauth2/service/OAuth2UserService.java`

`loadUser()` 완료 시점, 기존 사용자 로그인 분기에 추가:

```
OrganizationMemberJpaRepository.findAllByGithubUserId(githubId)
→ status == NOT_JOINED 인 항목 필터
→ member.link(user.getId()) 호출 → LINKED + joinedAt 설정
```

의존성: `OrganizationMemberJpaRepository` 주입 추가

---

## Step 6. OAuth scope 추가 및 재인증 처리

### 6-1. application.yml 수정

**수정 파일**: `backend/src/main/resources/application.yml`

```yaml
# 변경 전
scope: read:user, user:email, repo

# 변경 후
scope: read:user, user:email, repo, read:org
```

### 6-2. 재인증 에러 흐름

`GithubOrgApiClient`에서 403 응답 수신
→ `GlobalException(GITHUB_REAUTH_REQUIRED)` throw
→ 프론트에서 해당 에러 코드 수신 시 GitHub OAuth 재로그인 유도

---

## Step 7. 관리자 API

기존 admin 도메인에 추가하거나 Organization 도메인 내 `/v1/admin/organizations` 경로로 분리.

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| `GET` | `/v1/admin/organizations` | 전체 조직 목록 |
| `GET` | `/v1/admin/organizations/{id}` | 조직 상세 |
| `GET` | `/v1/admin/organizations/{id}/members` | 멤버 목록 (LINKED/NOT_JOINED 구분) |
| `GET` | `/v1/admin/organizations/{id}/repositories` | 저장소 목록 |
| `POST` | `/v1/admin/organizations/{id}/sync` | 수동 동기화 |
| `PATCH` | `/v1/admin/organizations/{id}/deactivate` | 조직 비활성화 |

권한: `@Permit(name = "조직 관리")` — canAccessAdmin 역할 필요.

---

## Step 8. 기능명세서.md 업데이트

아래 행을 기존 명세서에 추가:

| 기능 ID | 대분류 | 소분류 | 기능명 | 사용자 유형 | 기능 상세 | 상태 |
|---------|--------|--------|--------|------------|----------|------|
| ORG-001 | 조직 | 조직 등록 | Owner 조직 목록 조회 | 학생, 교직원 | GitHub에서 Owner 권한을 가진 조직 목록 조회. read:org 없으면 재인증 유도. | 대기중 |
| ORG-002 | 조직 | 조직 등록 | 조직 등록 | 학생, 교직원 | 선택한 GitHub Organization을 K-OSP에 등록. 멤버·저장소 자동 수집. | 대기중 |
| ORG-003 | 조직 | 조직 조회 | 내 등록 조직 목록 조회 | 학생, 교직원 | 본인이 등록한 조직 목록 조회. | 대기중 |
| ORG-004 | 조직 | 조직 조회 | 조직 상세 조회 | 학생, 교직원 | 조직 기본 정보, 멤버 수, 저장소 수 조회. | 대기중 |
| ADM-019 | 관리자 | 조직 관리 | 전체 조직 목록 조회 | 관리자 | 등록된 전체 조직 목록. | 대기중 |
| ADM-020 | 관리자 | 조직 관리 | 조직 멤버 목록 조회 | 관리자 | 조직별 멤버, LINKED/NOT_JOINED 상태 포함. | 대기중 |
| ADM-021 | 관리자 | 조직 관리 | 조직 저장소 목록 조회 | 관리자 | 조직별 저장소 목록. | 대기중 |
| ADM-022 | 관리자 | 조직 관리 | 조직 수동 동기화 | 관리자 | 선택 조직 멤버·저장소 재동기화. | 대기중 |
| ADM-023 | 관리자 | 조직 관리 | 조직 비활성화 | 관리자 | 조직 상태를 DISCONNECTED로 변경. | 대기중 |

---

## 참고: 생성·수정 파일 목록

```
[flyway]
V17__create_organizations.sql
V18__create_organization_members.sql
V19__create_organization_repositories.sql

[common - 신규]
common/.../organization/model/Organization.java
common/.../organization/model/OrganizationMember.java
common/.../organization/model/OrganizationRepository.java
common/.../organization/model/OrganizationStatus.java
common/.../organization/model/OrganizationMemberRole.java
common/.../organization/model/OrganizationMemberStatus.java
common/.../organization/repository/OrganizationJpaRepository.java
common/.../organization/repository/OrganizationRepository.java
common/.../organization/repository/OrganizationMemberJpaRepository.java
common/.../organization/repository/OrganizationRepositoryJpaRepository.java

[backend - infra 신규]
backend/.../infra/github/GithubOrgApiClient.java
backend/.../infra/github/dto/GithubOrgMembership.java
backend/.../infra/github/dto/GithubOrgSummary.java
backend/.../infra/github/dto/GithubOrgMember.java
backend/.../infra/github/dto/GithubOrgRepo.java

[backend - domain 신규]
backend/.../domain/organization/api/OrganizationApi.java
backend/.../domain/organization/controller/OrganizationController.java
backend/.../domain/organization/service/OrganizationService.java
backend/.../domain/organization/dto/request/OrganizationRegisterRequest.java
backend/.../domain/organization/dto/response/AvailableOrganizationResponse.java
backend/.../domain/organization/dto/response/OrganizationResponse.java
backend/.../domain/organization/dto/response/OrganizationDetailResponse.java

[backend - 수정]
backend/.../domain/auth/oauth2/service/OAuth2UserService.java
backend/.../resources/application.yml
backend/.../global/exception/ExceptionMessage.java
```

---

## 주요 정책 요약

- Owner 확인 기준: GitHub API 응답 `role == "admin"` (GitHub는 조직 owner를 admin으로 반환함)
- 중복 등록 방지: `github_org_id` UNIQUE 제약 + 서비스 레이어 검증
- 사용자 매핑 기준: `github_user_id` (Long) 우선, `github_username`은 표시용
- 비공개 조직 멤버 누락 가능 → 프론트엔드 안내 문구 필요
- 기존 사용자 토큰에 `read:org` 없을 시 → `GITHUB_REAUTH_REQUIRED` 에러 반환

---

## 개발일지

### Step 3 — backend infra GitHub Org API 클라이언트 (2026-06-23)
- DTO 4개: GithubOrgMembership, GithubOrgSummary, GithubOrgMember, GithubOrgRepo (record 타입)
  - `avatar_url`, `full_name`, `html_url` 필드에 `@JsonProperty` 적용 (GitHub API snake_case 대응)
- GithubOrgApiClient: WebClient 기반 경량 클라이언트, harvester와 독립
  - getMyOrgMemberships: bodyToFlux 사용
  - getOrgMembers / getOrgRepos: bodyToMono(ParameterizedTypeReference) 사용
  - 403/429 → GITHUB_REAUTH_REQUIRED, 404 → ORGANIZATION_NOT_FOUND 분리 처리
  - .block()으로 동기 호출 (Spring MVC 서블릿 컨텍스트)
- build.gradle.kts 수정 없음: webflux 이미 포함되어 있음
- 다음 Step 연결: OrganizationService에서 이 클라이언트를 주입받아 사용

### Step 2 — common 모듈 엔티티·레포지토리 (2026-06-23)
- Enum 3개: OrganizationStatus, OrganizationMemberRole, OrganizationMemberStatus
- 엔티티 3개: Organization, OrganizationMember, OrganizationRepo
  - OrganizationRepo로 명명 (OrganizationRepository는 Spring Data Repository와 혼동 방지)
  - OrganizationMember.userId는 Long 컬럼 (nullable FK, JPA 관계 없음) — Small Entities 원칙
  - OrganizationMember 빌더에서 userId 유무로 status/joinedAt 자동 결정
- 레포지토리 3개: OrganizationRepository, OrganizationMemberRepository, OrganizationRepoRepository
  - saveAll 선언: OrganizationMember/OrganizationRepo는 일괄 저장 필요하므로 포함
- ExceptionMessage 5개 추가: ORGANIZATION_NOT_FOUND, ORGANIZATION_ALREADY_REGISTERED, ORGANIZATION_OWNER_REQUIRED, ORGANIZATION_MEMBER_NOT_FOUND, GITHUB_REAUTH_REQUIRED
- 다음 Step 연결: backend infra에 GithubOrgApiClient 작성 시 이 엔티티들을 조립

### Step 1 — Flyway 마이그레이션 (2026-06-23)
- V17__create_organizations.sql: organizations 테이블 생성 (github_org_id UNIQUE, registered_by_user_id FK)
- V18__create_organization_members.sql: organization_members 테이블 생성 (user_id nullable, github_user_id 인덱스 추가)
- V19__create_organization_repositories.sql: organization_repositories 테이블 생성 (github_repo_id UNIQUE)
- 기존 V1 스타일 준수: `GENERATED BY DEFAULT AS IDENTITY`, 제약명 `pk_/uc_/fk_/idx_` 패턴
- `github_user_id` 인덱스는 Step 5 로그인 자동 매핑 시 성능을 위해 선제 추가
- 다음 Step 연결: common 모듈에서 이 테이블들과 매핑되는 엔티티 작성
