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
Step 1. Flyway 마이그레이션 (V17~V19) — DB 스키마(완료)
Step 2. common 모듈 — Organization 엔티티·레포지토리(완료)
Step 3. backend infra — GitHub Org API 클라이언트(완료)
Step 4. backend domain — Organization 서비스·컨트롤러·DTO(완료)
Step 5. backend — OAuth2UserService 자동 매핑 훅(완료)
Step 6. backend — OAuth scope 추가 및 재인증 처리(완료)
Step 7. backend — 관리자 API(완료)
Step 8. 기능명세서.md 업데이트(완료)
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

### Step 8 — 기능명세서.md 업데이트 (2026-06-23)
> 조직 등록·관리 관련 기능 9개를 기능명세서에 등록하고 전체 구현을 마무리했다.
- 기능명세서.md에 ORG-001~ORG-004, ADM-019~ADM-023 총 9개 항목 추가
- 최종 업데이트 날짜 갱신 (2026-05-20 → 2026-06-23)
- 기존 ADM-018 다음 행에 순서대로 삽입
- 모든 Step 구현 완료 — 조직 등록 기능 전체 백엔드 구현 마무리

### Step 7 — 관리자 API (2026-06-23)
> 관리자가 조직 목록 조회, 멤버·저장소 조회, 수동 동기화, 비활성화를 수행할 수 있는 Admin API를 구현했다.
- OrganizationRepository에 findAll() 선언 누락 버그 수정 (AdminOrganizationService.getAllOrganizations() 에서 호출)
- 응답 DTO 2개: AdminOrganizationMemberResponse, AdminOrganizationRepoResponse (record + from() 팩토리)
- AdminOrganizationService: getAllOrganizations, getMembers, getRepositories, sync, deactivate
  - sync: resyncMembers + resyncRepositories (재동기화 로직 — 초기 등록과 달리 REMOVED 상태 처리 포함)
  - resyncMembers: GitHub에 없는 멤버 → REMOVED, 신규 멤버 → saveAll
  - resyncRepositories: GitHub에 없는 저장소 → deactivate, 신규 저장소 → saveAll
  - TextEncryptor로 등록자의 GitHub 토큰 복호화하여 API 호출
- AdminOrganizationApi: Swagger 인터페이스 (5개 엔드포인트)
- AdminOrganizationController: implements AdminOrganizationApi, @Permit 적용
  - DELETE /{organizationId} → 204 No Content
  - POST /{organizationId}/sync → 200 OK
- 패키지: domain/admin/organization/{api,controller,dto/response,service}
- 다음 Step 연결: 기능명세서.md 업데이트

### Step 6 — OAuth scope 추가 및 재인증 처리 (2026-06-23)
> GitHub OAuth scope에 read:org를 추가하고, 기존 토큰에 권한이 없을 때의 재인증 에러 흐름을 완성했다.
- application.yml: scope에 read:org 추가 (read:user, user:email, repo, read:org)
- 재인증 흐름 전체 확인:
  - 기존 사용자 토큰에 read:org 없을 때 GitHub API → 403
  - GithubOrgApiClient.onStatus(403/429) → GlobalException(GITHUB_REAUTH_REQUIRED) (Step 3 완료)
  - GlobalExceptionHandler → HTTP 403 + 에러 메시지 (기존 코드)
  - 신규 로그인 시 read:org 포함 토큰 자동 발급 (이번 scope 추가)
- 백엔드에서 할 수 있는 재인증 처리 모두 완료, 프론트는 GITHUB_REAUTH_REQUIRED 에러 코드 기준으로 재로그인 유도 처리 필요
- 다음 Step 연결: 관리자 API 구현

### Step 5 — OAuth2UserService 자동 매핑 훅 (2026-06-23)
> 사용자가 GitHub 로그인할 때 NOT_JOINED 상태의 조직 멤버를 자동으로 LINKED 처리하는 훅을 붙였다.
- 수정 파일: OAuth2UserService.java
- buildLoginAttributes()에 autoLinkOrganizationMemberships(user) 호출 추가
  - 탈퇴 사용자·신규 사용자 분기에는 발동하지 않음 (의도된 동작)
- autoLinkOrganizationMemberships(): NOT_JOINED 상태 멤버를 github_user_id 기준으로 조회 후 link() 호출
- JPA dirty checking 활용: loadUser()가 @Transactional 내부이므로 별도 save() 불필요
- 주입 추가: OrganizationMemberRepository (List import도 함께 추가)
- 다음 Step 연결: application.yml에 read:org scope 추가 + 재인증 에러 흐름 완성

### Step 4 — backend domain Organization 서비스·컨트롤러·DTO (2026-06-23)
> 조직 등록·조회의 핵심 도메인 로직(서비스)과 REST API(컨트롤러, DTO)를 구현했다.
- DTO 3개: OrganizationRegisterRequest, AvailableOrganizationResponse, OrganizationResponse, OrganizationDetailResponse
  - 모두 record 타입, from() 정적 팩토리 메서드 적용
- OrganizationService:
  - getAvailableOrganizations: GitHub API 호출 후 role=="admin" 필터 + 등록 여부 표시
  - registerOrganization: Owner 검증 → Organization 저장 → syncMembers → syncRepositories
  - syncMembers: findAllByGithubIds로 기존 K-OSP 사용자 일괄 매핑
  - private 메서드로 분리해 메서드 길이 10줄 이하 준수
  - TextEncryptor 주입해 GitHub 토큰 복호화
- OrganizationApi: Swagger 인터페이스 (4개 엔드포인트)
- OrganizationController: implements OrganizationApi, @Permit 적용
  - POST /v1/organizations → 201 Created + Location 헤더
- 다음 Step 연결: OAuth2UserService에 로그인 시 NOT_JOINED → LINKED 자동 매핑 훅 추가

### Step 3 — backend infra GitHub Org API 클라이언트 (2026-06-23)
> GitHub REST API를 호출하는 경량 WebClient 기반 클라이언트와 응답 DTO를 구현했다.
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
> 조직·멤버·저장소 JPA 엔티티와 레포지토리 인터페이스, 예외 메시지를 common 모듈에 추가했다.
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
> organizations, organization_members, organization_repositories 세 테이블을 DB에 추가했다.
- V17__create_organizations.sql: organizations 테이블 생성 (github_org_id UNIQUE, registered_by_user_id FK)
- V18__create_organization_members.sql: organization_members 테이블 생성 (user_id nullable, github_user_id 인덱스 추가)
- V19__create_organization_repositories.sql: organization_repositories 테이블 생성 (github_repo_id UNIQUE)
- 기존 V1 스타일 준수: `GENERATED BY DEFAULT AS IDENTITY`, 제약명 `pk_/uc_/fk_/idx_` 패턴
- `github_user_id` 인덱스는 Step 5 로그인 자동 매핑 시 성능을 위해 선제 추가
- 다음 Step 연결: common 모듈에서 이 테이블들과 매핑되는 엔티티 작성

---

---

# 프론트엔드 조직 기능 구현 계획

> 작성일: 2026-06-28
> 목표: 백엔드 API(Step 1~8 완료) 기반으로 조직 등록·조회·상세·관리자 기능을 Next.js App Router로 구현

---

## Claude 행동 요령 (프론트엔드)

### 트리거
- 사용자가 `fe-step1`, `fe-step2`, ... `fe-step7` 을 입력하면 해당 Step을 구현한다.

### 구현 절차 (매 Step 공통)

```
1. 이 문서에서 해당 Step 내용을 읽는다.
2. 관련된 기존 코드를 먼저 읽고 파악한다.
3. 코드를 작성한다.
4. 작성 완료 후 스스로 재검토한다:
   - Server/Client 컴포넌트 분리 컨벤션 준수 여부 (page.tsx = Server, *Client.tsx = Client)
   - apiClient vs clientApiClient 올바른 사용 여부 (인증 필요 API는 clientApiClient)
   - 타입 누락 없는지 (types.ts 또는 해당 api 파일 내 정의)
   - 미들웨어 보호 라우트 추가 여부
   - 누락된 파일 없는지
5. 개발일지 항목을 이 문서 하단 [프론트엔드 개발일지] 섹션에 추가한다.
6. 개발일지 작성 완료 후 커밋&푸시한다:
   - git add . (해당 Step 파일만)
   - git commit -m "조직등록 fe-stepN"
   - git push origin develop
```

---

## 전체 구현 순서

```
fe-step1. API 클라이언트 + 타입 정의
fe-step2. 공통 컴포넌트 (OrganizationCard, OrganizationStatusBadge)
fe-step3. 내 조직 목록 페이지 (/organization)
fe-step4. 조직 등록 페이지 (/organization/register)
fe-step5. 조직 상세 페이지 (/organization/[id])
fe-step6. 미들웨어 보호 + 네비게이션 연결
fe-step7. 관리자 조직 관리 페이지 (/admin/organizations)
```

---

## fe-step1. API 클라이언트 + 타입 정의

### 신규 파일

**`Frontend/src/lib/api/organization.ts`**

> 인증이 필요한 모든 엔드포인트이므로 accessToken을 파라미터로 받음.
> Server Component(page.tsx)에서 호출 시 → `apiClient` + accessToken 옵션 사용 (tokenManager가 window를 참조하는 clientApiClient는 SSR에서 오류 발생).
> Client Component에서 호출 시 → `clientApiClient` + accessToken 옵션 사용.
> team.ts 패턴 동일: `getMyTeam`이 `apiClient(url, { accessToken })` 사용.

| 함수 | HTTP | 엔드포인트 | 클라이언트 | 설명 |
|------|------|-----------|-----------|------|
| `getAvailableOrganizations(accessToken)` | GET | `/v1/organizations/available` | `apiClient` | GitHub Owner 조직 목록 |
| `registerOrganization(data, accessToken)` | POST | `/v1/organizations` | `clientApiClient` | 조직 등록 (Client Component 전용) |
| `getMyOrganizations(accessToken)` | GET | `/v1/organizations/my` | `apiClient` | 내가 등록한 조직 목록 |
| `getOrganizationDetail(id, accessToken)` | GET | `/v1/organizations/{id}` | `apiClient` | 조직 상세 |

**타입 정의 위치**: `organization.ts` 파일 상단에 함께 선언 (types.ts가 아닌 파일 내부)

```typescript
export interface AvailableOrganizationResponse {
  githubOrgId: number;
  githubOrgName: string;
  avatarUrl: string | null;
  alreadyRegistered: boolean;
}

export interface OrganizationResponse {
  id: number;
  githubOrgId: number;
  githubOrgName: string;
  displayName: string;
  avatarUrl: string | null;
  status: 'ACTIVE' | 'PENDING' | 'DISCONNECTED';
  createdAt: string;
}

export interface OrganizationDetailResponse {
  id: number;
  githubOrgName: string;
  displayName: string;
  avatarUrl: string | null;
  status: 'ACTIVE' | 'PENDING' | 'DISCONNECTED';
  totalMemberCount: number;
  linkedMemberCount: number;
  repositoryCount: number;
  createdAt: string;
}

export interface OrganizationRegisterRequest {
  githubOrgId: number;
}
```

**`Frontend/src/lib/api/index.ts` 수정**
- organization 관련 함수 re-export 추가

---

## fe-step2. 공통 컴포넌트

**신규 디렉토리**: `Frontend/src/common/components/organization/`

### OrganizationCard.tsx

- `OrganizationResponse`를 props로 받아 카드 UI 렌더링
- GitHub 아바타 이미지 (`next/image`, fallback: 기본 아이콘)
- 상태 뱃지 (`OrganizationStatusBadge`) 포함
- `href` prop으로 Link 감싸기 → `/organization/{id}`
- 패턴 참고: `common/components/team/TeamCard.tsx`

### OrganizationStatusBadge.tsx

- `status: 'ACTIVE' | 'PENDING' | 'DISCONNECTED'` prop
- 색상 매핑:
  - `ACTIVE` → 초록
  - `PENDING` → 노랑
  - `DISCONNECTED` → 회색
- 패턴 참고: `common/components/StatusTag/`

---

## fe-step3. 내 조직 목록 페이지

**라우트**: `/organization`

### 파일 목록

| 파일 | 컴포넌트 유형 | 역할 |
|------|-------------|------|
| `app/organization/layout.tsx` | Server | 공통 Header/Footer |
| `app/organization/page.tsx` | Server | 세션 확인 → Client에 전달 |
| `app/organization/OrganizationPageClient.tsx` | Client | 조직 목록 렌더링, 등록 버튼 |

### page.tsx 흐름

```
1. auth() → session 획득  (import { auth } from '@/lib/auth/server')
2. session이 null이면 /login 리다이렉트
3. getMyOrganizations(session.accessToken) 호출
4. <OrganizationPageClient initialOrganizations={...} accessToken={...} /> 렌더링
```

### OrganizationPageClient.tsx 기능

- 조직 카드 목록 (`OrganizationCard` 사용)
- 조직이 없을 때 빈 상태 UI ("등록된 조직이 없습니다" + 등록하기 버튼)
- 우측 상단 "조직 등록하기" 버튼 → `/organization/register` 이동

---

## fe-step4. 조직 등록 페이지

**라우트**: `/organization/register`

### 파일 목록

| 파일 | 컴포넌트 유형 | 역할 |
|------|-------------|------|
| `app/organization/register/page.tsx` | Client | 전체 등록 플로우 |

### page.tsx 기능 (Client Component, `'use client'`)

```
1. useSession()으로 accessToken 획득
2. 마운트 시 getAvailableOrganizations(accessToken) 호출
3. 로딩 상태 표시
4. 조직 목록 카드 렌더링:
   - 아바타 + 조직명
   - alreadyRegistered === true → "이미 등록됨" 뱃지 + 선택 비활성화
   - alreadyRegistered === false → 선택 가능
5. "등록하기" 버튼 클릭 → registerOrganization({ githubOrgId }) 호출
6. 성공 → /organization 이동 (router.push)
7. GITHUB_REAUTH_REQUIRED 에러 → GitHub 재로그인 안내 토스트 표시
```

### 에러 처리

| 에러 코드 | 처리 방식 |
|----------|---------|
| `GITHUB_REAUTH_REQUIRED` | "GitHub 권한 재인증이 필요합니다" 토스트 + GitHub OAuth 재로그인 유도 |
| `ORGANIZATION_ALREADY_REGISTERED` | "이미 등록된 조직입니다" 토스트 |
| `ORGANIZATION_OWNER_REQUIRED` | "조직 Owner만 등록할 수 있습니다" 토스트 |

---

## fe-step5. 조직 상세 페이지

**라우트**: `/organization/[id]`

### 파일 목록

| 파일 | 컴포넌트 유형 | 역할 |
|------|-------------|------|
| `app/organization/[id]/page.tsx` | Server | 데이터 페치 → Client 전달 |
| `app/organization/[id]/OrganizationDetailClient.tsx` | Client | 상세 정보 렌더링 |

### page.tsx 흐름

```
1. auth() → session 획득  (import { auth } from '@/lib/auth/server')
2. session이 null이면 /login 리다이렉트
3. getOrganizationDetail(id, session.accessToken) 호출
4. <OrganizationDetailClient detail={...} /> 렌더링
```

### OrganizationDetailClient.tsx 표시 항목

| 항목 | 내용 |
|------|------|
| 아바타 + 조직명 | displayName (githubOrgName fallback) |
| 상태 뱃지 | OrganizationStatusBadge |
| 통계 카드 | 전체 멤버 수, K-OSP 연동 멤버 수, 저장소 수 |
| 등록일 | createdAt 포맷 (YYYY.MM.DD) |
| GitHub 링크 | `https://github.com/{githubOrgName}` 외부 링크 |

---

## fe-step6. 미들웨어 보호 + 네비게이션 연결

### 수정 파일: `Frontend/src/middleware.ts`

`PROTECTED_ROUTES` 배열과 `config.matcher` **둘 다** 추가해야 함. matcher에 없으면 미들웨어 자체가 실행되지 않음.

```typescript
// PROTECTED_ROUTES 배열에 추가
'/organization',

// config.matcher 배열에 추가
'/organization/:path*',
```

### 수정 파일: 헤더 네비게이션 컴포넌트

- 로그인 상태일 때 "내 조직" 링크 추가 (`/organization`)
- 기존 헤더 파일 위치 확인 후 수정 (`common/components/Header/`)

---

## fe-step7. 관리자 조직 관리 페이지

**라우트**: `/admin/organizations`

### 파일 목록

| 파일 | 컴포넌트 유형 | 역할 |
|------|-------------|------|
| `app/admin/organizations/page.tsx` | Client | 전체 조직 목록 |
| `app/admin/organizations/[id]/page.tsx` | Client | 조직 상세 + 멤버/저장소 탭 |

### 신규 API 함수 (`lib/api/organization.ts` 에 추가)

> ⚠ 백엔드 AdminOrganizationController 실제 구현 기준.
> - 조직 상세(`GET /v1/admin/organizations/{id}`) 엔드포인트는 **존재하지 않음**.
>   → 어드민 상세 페이지의 기본 정보 헤더는 `GET /v1/organizations/{id}` (기존 `getOrganizationDetail`) 재사용.
> - deactivate는 **PATCH가 아닌 DELETE** (`DELETE /v1/admin/organizations/{id}`).

| 함수 | HTTP | 엔드포인트 |
|------|------|-----------|
| `getAdminOrganizations(accessToken)` | GET | `/v1/admin/organizations` |
| `getAdminOrganizationMembers(id, accessToken)` | GET | `/v1/admin/organizations/{id}/members` |
| `getAdminOrganizationRepositories(id, accessToken)` | GET | `/v1/admin/organizations/{id}/repositories` |
| `syncOrganization(id, accessToken)` | POST | `/v1/admin/organizations/{id}/sync` |
| `deactivateOrganization(id, accessToken)` | DELETE | `/v1/admin/organizations/{id}` |

모두 `clientApiClient` 사용 (어드민 페이지는 전체 Client Component).
조직 기본 정보: 기존 `getOrganizationDetail(id, accessToken)` 재사용.

### admin/organizations/page.tsx 기능

- useSession() → accessToken
- 조직 목록 테이블 (조직명, 상태, 멤버 수, 등록일)
- 행 클릭 → `/admin/organizations/{id}`

### admin/organizations/[id]/page.tsx 기능

- 조직 기본 정보 헤더
- 탭: "멤버" / "저장소"
- 멤버 탭: github_username, role, status(LINKED/NOT_JOINED/REMOVED), joinedAt
- 저장소 탭: repository_full_name, visibility, is_active
- 우측 상단 버튼:
  - "동기화" → syncOrganization() 호출 후 데이터 갱신
  - "비활성화" → 확인 다이얼로그 → deactivateOrganization() 호출
- 패턴 참고: `app/admin/users/list/[id]/page.tsx`

### 수정 파일: `app/admin/layout.tsx`

navigation 배열에 추가:
```typescript
{ name: '조직 관리', href: '/admin/organizations', icon: Building2 }
```

---

## 생성·수정 파일 목록 (프론트엔드)

```
[신규]
Frontend/src/lib/api/organization.ts
Frontend/src/common/components/organization/OrganizationCard.tsx
Frontend/src/common/components/organization/OrganizationStatusBadge.tsx
Frontend/src/app/organization/layout.tsx
Frontend/src/app/organization/page.tsx
Frontend/src/app/organization/OrganizationPageClient.tsx
Frontend/src/app/organization/register/page.tsx
Frontend/src/app/organization/[id]/page.tsx
Frontend/src/app/organization/[id]/OrganizationDetailClient.tsx
Frontend/src/app/admin/organizations/page.tsx
Frontend/src/app/admin/organizations/[id]/page.tsx

[수정]
Frontend/src/lib/api/index.ts                  (organization 함수 re-export)
Frontend/src/middleware.ts                     (/organization 보호 라우트 추가)
Frontend/src/common/components/Header/        (내 조직 네비게이션 링크 추가)
Frontend/src/app/admin/layout.tsx              (조직 관리 사이드바 항목 추가)
```

---

## 프론트엔드 개발일지

### fe-step7 — 관리자 조직 관리 페이지 (2026-06-28)
- `app/admin/layout.tsx` 수정: Building2 아이콘 import, navigation에 '조직 관리' 항목 추가
- `app/admin/organizations/page.tsx` 신규 생성: 전체 조직 테이블 (행 클릭 → 상세)
- `app/admin/organizations/[id]/page.tsx` 신규 생성:
  - 조직 헤더(통계 3개) + 동기화/비활성화 버튼
  - 멤버 탭: githubUsername, role, status(LINKED/NOT_JOINED/REMOVED), joinedAt, syncedAt
  - 저장소 탭: repositoryFullName(GitHub 링크), visibility, isActive, syncedAt
  - 비활성화 확인 모달 + 완료 후 목록 페이지 이동
  - getOrganizationDetail 재사용 (admin 전용 상세 엔드포인트 없으므로)
- 프론트엔드 전체 구현 완료 (fe-step1 ~ fe-step7)

### fe-step6 — 미들웨어 보호 + 네비게이션 연결 (2026-06-28)
- `middleware.ts` 수정: PROTECTED_ROUTES에 `/organization` 추가, config.matcher에 `/organization/:path*` 추가
- `common/components/Header/index.tsx` 수정:
  - 데스크탑 드롭다운: "이력서" 다음에 "내 조직" Menu.Item(Link) 추가
  - 모바일 프로필 메뉴: 관리자 항목 위에 "내 조직" Link 추가 (로그인 시 항상 표시)
- 다음 Step 연결: fe-step7 관리자 조직 관리 페이지 구현

### fe-step5 — 조직 상세 페이지 (2026-06-28)
- `app/organization/[id]/page.tsx`: Server Component — auth() 세션 확인, getOrganizationDetail() 호출. 404 → notFound(), 미인증 → /login 리다이렉트
- `app/organization/[id]/OrganizationDetailClient.tsx`: Client Component
  - 조직 헤더(아바타, 이름, 상태 뱃지, GitHub 외부 링크, 등록일)
  - 통계 카드 3개(전체 멤버, K-OSP 연동, 저장소 수)
  - K-OSP 멤버 연동률 프로그레스바
- 다음 Step 연결: fe-step6 미들웨어 보호 + 네비게이션 연결

### fe-step4 — 조직 등록 페이지 (2026-06-28)
- `app/organization/register/page.tsx` 신규 생성 (Client Component)
  - useSession()으로 토큰 취득, 마운트 시 getAvailableOrganizations() 호출
  - 카드 선택 UI: 아바타 + 조직명, alreadyRegistered → 비활성 + "이미 등록됨" 뱃지
  - 선택 확정 → registerOrganization() → /organization 이동
  - 에러 처리: 403(GITHUB_REAUTH_REQUIRED), ORGANIZATION_ALREADY_REGISTERED, ORGANIZATION_OWNER_REQUIRED 각각 토스트 안내
  - 비공개 조직 멤버 누락 가능 안내 문구 포함
- 다음 Step 연결: fe-step5 조직 상세 페이지 구현

### fe-step3 — 내 조직 목록 페이지 (2026-06-28)
- `app/organization/layout.tsx`: AppHeader + Footer 공통 레이아웃
- `app/organization/page.tsx`: Server Component — auth() 세션 확인, getMyOrganizations() 호출 후 Client에 전달. 미인증 시 /login 리다이렉트
- `app/organization/OrganizationPageClient.tsx`: 조직 카드 그리드(2~3열), 빈 상태 UI, 조직 등록하기 버튼
- 다음 Step 연결: fe-step4 조직 등록 페이지 구현

### fe-step2 — 공통 컴포넌트 (2026-06-28)
- `OrganizationStatusBadge.tsx` 신규 생성
  - status → 한글 라벨 + 색상 매핑 (ACTIVE: 초록, PENDING: 노랑, DISCONNECTED: 회색)
  - StatusTag 패턴 동일하게 적용
- `OrganizationCard.tsx` 신규 생성
  - TeamCard 패턴 동일하게 적용 (Link 감싸기, Image + fallback Building2 아이콘)
  - OrganizationStatusBadge 포함
  - href → `/organization/{id}`
- 다음 Step 연결: fe-step3 내 조직 목록 페이지 구현

### fe-step1 — API 클라이언트 + 타입 정의 (2026-06-28)
- `Frontend/src/lib/api/organization.ts` 신규 생성
  - 타입 7개: AvailableOrganizationResponse, OrganizationResponse, OrganizationDetailResponse, OrganizationRegisterRequest, AdminOrganizationMemberResponse, AdminOrganizationRepoResponse
  - 함수 9개: getAvailableOrganizations, registerOrganization, getMyOrganizations, getOrganizationDetail, getAdminOrganizations, getAdminOrganizationMembers, getAdminOrganizationRepositories, syncOrganization, deactivateOrganization
  - Server Component 호출 함수(get류)는 `apiClient` 사용, Client Component 전용(register/sync/deactivate)은 `clientApiClient` 사용
- `Frontend/src/lib/api/index.ts` 수정: `export * from './organization'` 추가
- 다음 Step 연결: fe-step2 공통 컴포넌트(OrganizationCard, OrganizationStatusBadge) 구현
