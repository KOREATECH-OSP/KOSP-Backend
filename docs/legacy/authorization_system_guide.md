# 인증 시스템 가이드 (Redis Pub/Sub RBAC + @Permit)

## 개요
AWS IAM(Role-Policy-Permission)의 유연함과 Local Cache + Redis Pub/Sub의 고성능을 결합한 실시간 RBAC 시스템입니다.
또한, 개발 편의성과 보안 관리를 위해 **통합 어노테이션(`@Permit`)** 기반의 권한 제어를 제공합니다.

## 핵심 기능
1.  **Zero Overhead**: 일반적인 요청은 로컬 메모리 체크만 수행(나노초 단위).
2.  **Real-Time Updates**: Redis Pub/Sub을 통해 권한 변경 사항이 전파되고, 자동 리로드됩니다.
3.  **Unified Control**: `@Permit` 어노테이션 하나로 공개/비공개/권한체크를 모두 관리합니다.
4.  **Auto Initialization**: 코드에 정의된 권한을 서버 시작 시 DB에 자동으로 등록합니다.

## 아키텍처

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Aspect (@Permit)
    participant SecurityContext
    
    Client->>Controller: API 요청 (GET /articles)
    Controller->>Aspect: Intercept (@Permit)
    
    alt permitAll = true
        Aspect->>Controller: Pass (공개 접근)
    else permitAll = false
        Aspect->>SecurityContext: isAuthenticated?
        
        opt Not Authenticated
            Aspect-->>Client: 401 Unauthorized
        end
        
        alt name is empty
            Aspect->>Controller: Pass (로그인만 필요)
        else name = "article:read"
            Aspect->>SecurityContext: hasAuthority("article:read")?
            
            alt Has Permission
                Aspect->>Controller: Pass (권한 있음)
            else No Permission
                Aspect-->>Client: 403 Forbidden
            end
        end
    end
    
    Controller-->>Client: 응답
```

## 구성 요소

### 1. 보안 (Security)
*   `@Permit`: 통합 보안 어노테이션.
*   `PermissionCheckAspect`: `@Permit`을 해석하여 권한을 검사하는 AOP Aspect.
*   `ReloadAuthenticationFilter`: Redis Pub/Sub 신호를 감지하여 권한을 실시간으로 갱신.
*   `UserDetailsServiceImpl`: DB에서 Role -> Policy -> Permission 구조를 로드.

### 2. 인프라 (Infrastructure)
*   **PermissionInitializer**: `@Permit` 어노테이션을 스캔하여 DB에 없는 권한을 자동 등록.
*   **BoardInitializer**: 기본 게시판 데이터 생성.

## 사용 방법

### 1. API 권한 정의 (@Permit)
컨트롤러 메소드 위에 직관적으로 정의합니다.

**공개 API (로그인 불필요)**
```java
@Permit(permitAll = true, description = "회원가입")
@PostMapping("/signup")
public ResponseEntity<Void> signup(...) { ... }
```

**로그인 유저 전용 (권한 불필요, 로그인만 체크)**
```java
@Permit(description = "내 정보 조회")
@GetMapping("/me")
public ResponseEntity<Response> getMyInfo(...) { ... }
```

**특정 권한 필요 (RBAC)**
```java
@Permit(name = "article:create", description = "게시글 작성")
@PostMapping
public ResponseEntity<Void> create(...) { ... }
```
*   `name`: 필요한 권한 키 (DB의 `Permission` 테이블과 매핑됨).
*   이 권한 키는 서버 시작 시 자동으로 DB에 등록됩니다(`PermissionInitializer`).

### 2. 권한 업데이트 (Admin)
관리자가 권한을 변경하면 시스템이 자동으로 인지하여 반영하므로, 별도의 재로그인이 필요 없습니다.
```java
// 예: 유저에게 Role 부여
adminService.assignRole(userId, "ROLE_WRITER"); 
// -> Redis Pub/Sub 발행 -> 모든 서버의 해당 유저 캐시 무효화 -> 다음 요청 시 자동 리로드
```
