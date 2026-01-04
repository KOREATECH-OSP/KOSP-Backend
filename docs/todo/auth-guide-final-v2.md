# 인증 시스템 구현 가이드 (최종)

> Next.js + NextAuth + Spring Boot + Spring Security
> 한국기술교육대학교 구성원 전용 서비스

---

## 1. 개요

### 1.1 회원가입 필드

| 필드 | 학생 | 교직원 | 필수 | 검증 규칙 |
|------|------|--------|------|-----------|
| 회원 유형 | ✅ | ✅ | ✅ | STUDENT / STAFF |
| 아이디 | ✅ | ✅ | ✅ | 4-20자, 영문+숫자 |
| 비밀번호 | ✅ | ✅ | ✅ | 8자 이상, 영문+숫자+특수문자 |
| 학번 | ✅ | - | ✅ (학생) | 10자리 숫자 |
| 사번 | - | ✅ | ✅ (교직원) | 6자리 또는 8자리 숫자 |
| 이름 | ✅ | ✅ | ✅ | 2-50자 |
| 이메일 | ✅ | ✅ | ✅ | @koreatech.ac.kr |
| GitHub 연동 | ✅ | ✅ | ✅ | OAuth 인증 |
| 이용약관 동의 | ✅ | ✅ | ✅ | true |
| 개인정보처리방침 동의 | ✅ | ✅ | ✅ | true |

### 1.2 로그인 방식

| 방식 | 설명 |
|------|------|
| 아이디/비밀번호 | 회원가입 시 설정한 아이디와 비밀번호로 로그인 |
| GitHub | 연동된 GitHub 계정으로 로그인 |

### 1.3 기술 스택

| 구분 | 기술 | 역할 |
|------|------|------|
| Frontend | Next.js 14+ | UI, 라우팅 |
| Frontend | NextAuth v5 | 로그인 세션 관리 (Credentials + GitHub) |
| Backend | Spring Boot 3.x | API 서버 |
| Backend | Spring Security | JWT 발급/검증, 비밀번호 암호화 |
| Database | PostgreSQL | 사용자 정보 저장 |
| Cache | Redis | 이메일 인증, GitHub 연동 토큰, Refresh Token |

---

## 2. 보안 설계 원칙

### 2.1 왜 인증 토큰이 필요한가?

회원가입 과정에서 이메일 인증과 GitHub 연동은 **별도의 비동기 작업**입니다. 사용자가 인증을 완료한 후 실제 회원가입 버튼을 누르기까지 시간 차이가 발생합니다.

이 시간 차이 동안 **서버는 클라이언트가 보내는 정보를 신뢰할 수 없습니다.**

#### 2.1.1 이메일 인증 토큰이 필요한 이유

**토큰 없이 이메일만 보내는 경우의 문제:**

```
1. 사용자 A가 자신의 이메일 user_a@koreatech.ac.kr로 인증 완료
2. 사용자 A가 회원가입 요청 시 이메일을 victim@koreatech.ac.kr로 변경
3. 서버는 이 이메일이 인증된 것인지 알 방법이 없음
4. → 사용자 A가 다른 사람의 이메일로 가입됨 🚨
```

**토큰으로 해결:**

```
1. 사용자가 user@koreatech.ac.kr로 인증 요청
2. 서버: 인증 성공 → 토큰 발급, Redis에 저장
   - Key: "email:token:abc123"
   - Value: "user@koreatech.ac.kr"
   - TTL: 30분
3. 회원가입 시 토큰과 이메일을 함께 전송
4. 서버: 토큰으로 Redis 조회 → 저장된 이메일과 요청 이메일 비교
5. 일치하면 인증된 이메일로 확인됨 ✅
```

#### 2.1.2 GitHub 연동 토큰이 필요한 이유

**토큰 없이 githubId만 보내는 경우의 문제:**

```
1. 사용자 A가 GitHub 연동 → githubId: "12345678" 획득
2. 사용자 A가 이 githubId를 사용자 B에게 알려줌
3. 사용자 B가 자기 회원가입 폼에 githubId: "12345678" 입력
4. 서버는 이 githubId가 실제로 인증된 것인지 알 방법이 없음
5. → 사용자 B가 사용자 A의 GitHub 계정으로 가입됨 🚨
6. → 사용자 A가 나중에 GitHub 로그인하면 사용자 B의 계정에 접근됨 🚨🚨
```

**토큰으로 해결:**

```
1. 사용자가 GitHub OAuth 완료
2. 서버: GitHub 정보 확인 → 토큰 발급, Redis에 저장
   - Key: "github:token:xyz789"
   - Value: {"githubId": "12345678", "githubUsername": "octocat", ...}
   - TTL: 30분
3. 회원가입 시 토큰과 githubId를 함께 전송
4. 서버: 토큰으로 Redis 조회 → 저장된 githubId와 요청 githubId 비교
5. 일치하면 실제로 인증된 GitHub 계정으로 확인됨 ✅
```

### 2.2 TTL(Time To Live)이 필요한 이유

토큰이 영구적이면 또 다른 보안 문제가 발생합니다:

```
1. 사용자가 공용 PC에서 이메일 인증만 하고 자리를 비움
2. 다른 사람이 그 토큰을 사용해서 회원가입
```

**TTL로 해결:**
- 토큰은 일정 시간 후 자동 만료
- 만료된 토큰으로는 회원가입 불가
- 사용자는 다시 인증해야 함

### 2.3 TTL 정책

| 항목 | TTL | Redis Key 패턴 | 만료 시 |
|------|-----|----------------|---------|
| 이메일 인증 코드 | 10분 | `email:verify:{email}` | 재발송 필요 |
| 이메일 인증 토큰 | 30분 | `email:token:{token}` | 이메일 재인증 필요 |
| GitHub 연동 토큰 | 30분 | `github:token:{token}` | GitHub 재연동 필요 |
| 이메일 발송 Rate Limit | 1분 | `email:ratelimit:{email}` | 재발송 가능 |
| 인증 시도 횟수 | 5분 | `email:attempt:{email}` | 시도 횟수 초기화 |
| Access Token | 30분 | JWT (저장 안함) | Refresh Token으로 갱신 |
| Refresh Token | 7일 | `refresh:{userId}` | 재로그인 필요 |

### 2.4 만료 시나리오

**시나리오: 사용자가 회원가입 폼을 30분 이상 방치 후 제출**

| 경과 시간 | 이메일 인증 토큰 | GitHub 연동 토큰 | 결과 |
|----------|-----------------|-----------------|------|
| 0분 | 발급 | - | - |
| 10분 | 유효 | 발급 | - |
| 30분 | 유효 | 유효 | 회원가입 가능 ✅ |
| 31분 | **만료** | 유효 | "이메일 인증이 만료되었습니다" ❌ |
| 40분 | 만료 | 유효 | "이메일 인증이 만료되었습니다" ❌ |
| 41분 | 만료 | **만료** | 이메일/GitHub 둘 다 재인증 필요 ❌ |

**권장 사용자 플로우:**
1. 이메일 인증 완료 후 **30분 이내**에 회원가입 완료
2. GitHub 연동 완료 후 **30분 이내**에 회원가입 완료
3. 만료된 경우 해당 인증만 다시 진행

---

## 3. 데이터 모델

### 3.1 User Entity

```java
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ===== 로그인 정보 =====
    @Column(unique = true, nullable = false, length = 20)
    private String username;  // 아이디
    
    @Column(nullable = false)
    private String password;  // BCrypt 암호화
    
    // ===== 회원 유형 =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType memberType;  // STUDENT, STAFF
    
    // ===== 식별자 =====
    @Column(unique = true, nullable = false)
    private String memberId;  // 학번(10자리) 또는 사번(6/8자리)
    
    // ===== 기본 정보 =====
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;  // @koreatech.ac.kr
    
    // ===== GitHub 연동 =====
    @Column(unique = true, nullable = false)
    private String githubId;
    
    @Column(nullable = false)
    private String githubUsername;
    
    private String githubEmail;
    
    // ===== 약관 동의 =====
    @Column(nullable = false)
    private Boolean termsAgreed = false;
    
    @Column(nullable = false)
    private Boolean privacyAgreed = false;
    
    private LocalDateTime termsAgreedAt;
    
    private LocalDateTime privacyAgreedAt;
    
    // ===== 계정 상태 =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    // ===== 시간 정보 =====
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

public enum MemberType {
    STUDENT,  // 학생
    STAFF     // 교직원
}

public enum UserStatus {
    ACTIVE,      // 활성
    INACTIVE,    // 비활성
    SUSPENDED    // 정지
}
```

### 3.2 Redis 데이터 구조

```
# 이메일 인증 코드
Key:   email:verify:user@koreatech.ac.kr
Value: "123456"
TTL:   10분

# 이메일 인증 토큰
Key:   email:token:550e8400-e29b-41d4-a716-446655440000
Value: "user@koreatech.ac.kr"
TTL:   30분

# GitHub 연동 토큰
Key:   github:token:7c9e6679-7425-40de-944b-e07fc1f90ae7
Value: {"githubId":"12345678","githubUsername":"octocat","githubEmail":"octocat@github.com"}
TTL:   30분

# 이메일 발송 Rate Limit
Key:   email:ratelimit:user@koreatech.ac.kr
Value: "1"
TTL:   1분

# 인증 시도 횟수
Key:   email:attempt:user@koreatech.ac.kr
Value: "3"
TTL:   5분

# Refresh Token
Key:   refresh:1
Value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
TTL:   7일
```

---

## 4. 회원가입 플로우

### 4.1 전체 흐름

| 단계 | 사용자 액션 | Frontend | Backend API | 서버 처리 | 응답 |
|------|------------|----------|-------------|----------|------|
| 1 | 회원 유형 선택 | memberType 상태 변경 | - | - | - |
| 2 | 이용약관 동의 | termsAgreed = true | - | - | - |
| 3 | 개인정보처리방침 동의 | privacyAgreed = true | - | - | - |
| 4 | 아이디 입력 + 중복확인 | 로딩 표시 | GET /api/auth/check-username | DB 조회 | { available } |
| 5 | 비밀번호 입력 | 실시간 형식 검증 | - | - | - |
| 6 | 비밀번호 확인 입력 | 일치 여부 검증 | - | - | - |
| 7 | 학번/사번 입력 + 중복확인 | 로딩 표시 | GET /api/auth/check-member-id | DB 조회 | { available } |
| 8 | 이름 입력 | 실시간 검증 | - | - | - |
| 9 | 이메일 입력 | @koreatech.ac.kr 검증 | - | - | - |
| 10 | 인증코드 발송 | 타이머 시작 | POST /api/auth/send-email | Redis 저장 + 메일 발송 | { expiresIn: 600 } |
| 11 | 인증코드 입력 + 확인 | 로딩 표시 | POST /api/auth/verify-email | Redis 검증 + **토큰 발급** | { emailVerificationToken, expiresIn: 1800 } |
| 12 | GitHub 연동 (팝업) | 팝업 열기 | POST /api/auth/github/exchange | GitHub API + **토큰 발급** | { githubId, githubVerificationToken, expiresIn: 1800 } |
| 13 | 회원가입 버튼 클릭 | 전체 데이터 전송 | POST /api/auth/signup | **토큰 검증** + User 생성 | { accessToken, user } |

### 4.2 회원가입 요청 데이터

```typescript
interface SignupRequest {
  // 약관 동의
  termsAgreed: boolean
  privacyAgreed: boolean
  
  // 로그인 정보
  username: string
  password: string
  
  // 회원 정보
  memberType: 'STUDENT' | 'STAFF'
  memberId: string      // 학번 또는 사번
  name: string
  email: string
  
  // 인증 토큰 (서버에서 발급받은 것)
  emailVerificationToken: string    // 이메일 인증 시 발급
  githubVerificationToken: string   // GitHub 연동 시 발급
  
  // GitHub 정보 (표시용, 토큰으로 검증됨)
  githubId: string
  githubUsername: string
  githubEmail: string | null
}
```

### 4.3 회원가입 버튼 활성화 조건

```typescript
const canSubmit = useMemo(() => {
  // 공통 조건
  const baseConditions = 
    formData.termsAgreed &&
    formData.privacyAgreed &&
    validation.isUsernameChecked &&
    validation.isUsernameAvailable &&
    validation.isPasswordValid &&
    validation.isPasswordMatch &&
    validation.isMemberIdChecked &&
    validation.isMemberIdAvailable &&
    formData.name.length >= 2 &&
    validation.isEmailVerified &&
    validation.isGithubLinked &&
    !loading.isSubmitting

  // 회원 유형별 추가 조건
  if (formData.memberType === 'STUDENT') {
    return baseConditions && /^\d{10}$/.test(formData.memberId)
  } else if (formData.memberType === 'STAFF') {
    return baseConditions && /^(\d{6}|\d{8})$/.test(formData.memberId)
  }
  
  return false
}, [formData, validation, loading])
```

---

## 5. 로그인 플로우

### 5.1 아이디/비밀번호 로그인

| 단계 | 주체 | 액션 |
|------|------|------|
| 1 | 사용자 | 아이디, 비밀번호 입력 |
| 2 | 사용자 | 로그인 버튼 클릭 |
| 3 | Frontend | NextAuth signIn("credentials") 호출 |
| 4 | NextAuth | Credentials Provider → Backend 호출 |
| 5 | Backend | POST /api/auth/login 처리 |
| 6 | Backend | 아이디로 사용자 조회, 비밀번호 검증 (BCrypt) |
| 7 | Backend | JWT 발급 (Access + Refresh) |
| 8 | NextAuth | 세션에 토큰 저장 |
| 9 | Frontend | 메인 페이지로 이동 |

### 5.2 GitHub 로그인

| 단계 | 주체 | 액션 |
|------|------|------|
| 1 | 사용자 | "GitHub로 로그인" 버튼 클릭 |
| 2 | Frontend | NextAuth signIn("github") 호출 |
| 3 | NextAuth | GitHub OAuth 페이지로 리다이렉트 |
| 4 | 사용자 | GitHub 로그인 |
| 5 | NextAuth | signIn callback 실행 |
| 6 | signIn callback | Backend POST /api/auth/github-login 호출 |
| 7 | Backend | GitHub ID로 사용자 조회 |
| 7-A | Backend | 사용자 없음 → 404 (회원가입 필요) |
| 7-B | Backend | 사용자 있음 → JWT 발급 |
| 8 | NextAuth | 세션에 토큰 저장 |
| 9 | Frontend | 메인 페이지로 이동 |

---

## 6. API 상세 명세

### 6.1 아이디 중복확인

**왜 필요한가?**
- 아이디는 로그인에 사용되는 고유 식별자
- 중복 아이디로 가입하면 로그인 시 충돌 발생
- 회원가입 전에 미리 확인하여 UX 개선

**Request**
```http
GET /api/auth/check-username?username=testuser
```

**Backend**
```java
@GetMapping("/check-username")
public ResponseEntity<?> checkUsername(@RequestParam String username) {
    // 1. 형식 검증
    if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "available", false,
            "message", "아이디는 4-20자의 영문, 숫자만 사용 가능합니다."
        ));
    }
    
    // 2. 중복 확인
    boolean exists = userRepository.existsByUsername(username);
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "available", !exists,
        "message", exists ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다."
    ));
}
```

**Response**
```json
// 사용 가능
{ "success": true, "available": true, "message": "사용 가능한 아이디입니다." }

// 중복
{ "success": true, "available": false, "message": "이미 사용중인 아이디입니다." }

// 형식 오류
{ "success": false, "available": false, "message": "아이디는 4-20자의 영문, 숫자만 사용 가능합니다." }
```

**Frontend 처리**
```typescript
const checkUsername = async () => {
  setLoading(prev => ({ ...prev, isCheckingUsername: true }))
  setError(prev => ({ ...prev, username: null }))
  
  try {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/auth/check-username?username=${formData.username}`
    )
    const data = await res.json()
    
    if (!res.ok || !data.available) {
      setError(prev => ({ ...prev, username: data.message }))
      setValidation(prev => ({ ...prev, isUsernameChecked: true, isUsernameAvailable: false }))
      return
    }
    
    setValidation(prev => ({ ...prev, isUsernameChecked: true, isUsernameAvailable: true }))
    
  } catch (err) {
    setError(prev => ({ ...prev, username: '서버 연결에 실패했습니다.' }))
  } finally {
    setLoading(prev => ({ ...prev, isCheckingUsername: false }))
  }
}
```

---

### 6.2 학번/사번 중복확인

**왜 필요한가?**
- 학번/사번은 실제 학교 구성원임을 증명하는 식별자
- 한 학번/사번으로 여러 계정 생성 방지
- 학생과 교직원은 식별자 형식이 다르므로 회원 유형과 함께 검증

**Request**
```http
GET /api/auth/check-member-id?type=STUDENT&id=2024136000
```

**Backend**
```java
@GetMapping("/check-member-id")
public ResponseEntity<?> checkMemberId(
    @RequestParam("type") MemberType type,
    @RequestParam("id") String memberId
) {
    // 1. 형식 검증
    String label = type == MemberType.STUDENT ? "학번" : "사번";
    
    if (type == MemberType.STUDENT) {
        if (!memberId.matches("^\\d{10}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "available", false,
                "message", "학번은 10자리 숫자여야 합니다."
            ));
        }
    } else {
        if (!memberId.matches("^(\\d{6}|\\d{8})$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "available", false,
                "message", "사번은 6자리 또는 8자리 숫자여야 합니다."
            ));
        }
    }
    
    // 2. 중복 확인
    boolean exists = userRepository.existsByMemberTypeAndMemberId(type, memberId);
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "available", !exists,
        "message", exists ? "이미 가입된 " + label + "입니다." : "사용 가능한 " + label + "입니다."
    ));
}
```

---

### 6.3 이메일 인증코드 발송

**왜 필요한가?**
- @koreatech.ac.kr 이메일 소유 확인
- 실제 학교 구성원만 가입 가능하도록 제한
- 이메일 도메인 검증만으로는 소유 여부 확인 불가

**왜 Rate Limiting이 필요한가?**
- 이메일 발송은 비용이 발생하는 작업
- 악의적인 사용자가 대량 발송 요청 가능
- 수신자 스팸함 등록 방지

**Request**
```http
POST /api/auth/send-email
Content-Type: application/json

{ "email": "user@koreatech.ac.kr" }
```

**Backend**
```java
@PostMapping("/send-email")
public ResponseEntity<?> sendEmail(@RequestBody @Valid SendEmailRequest request) {
    String email = request.getEmail();
    
    // 1. 도메인 검증 - @koreatech.ac.kr만 허용
    if (!email.endsWith("@koreatech.ac.kr")) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "code", "INVALID_EMAIL_DOMAIN",
            "message", "@koreatech.ac.kr 이메일만 사용 가능합니다."
        ));
    }
    
    // 2. 이미 가입된 이메일 확인
    if (userRepository.existsByEmail(email)) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "success", false,
            "code", "EMAIL_EXISTS",
            "message", "이미 가입된 이메일입니다."
        ));
    }
    
    // 3. Rate Limiting (1분 내 재발송 불가)
    String rateLimitKey = "email:ratelimit:" + email;
    if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
        Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
            "success", false,
            "code", "RATE_LIMITED",
            "message", ttl + "초 후에 다시 시도해주세요.",
            "retryAfter", ttl
        ));
    }
    
    // 4. 인증 코드 생성 (6자리 숫자)
    String code = String.format("%06d", new Random().nextInt(1000000));
    
    // 5. Redis에 저장 (10분 TTL)
    redisTemplate.opsForValue().set(
        "email:verify:" + email, 
        code, 
        10, TimeUnit.MINUTES
    );
    
    // 6. Rate Limit 설정 (1분)
    redisTemplate.opsForValue().set(
        rateLimitKey, 
        "1", 
        1, TimeUnit.MINUTES
    );
    
    // 7. 이메일 발송
    emailService.sendVerificationEmail(email, code);
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "인증 코드가 발송되었습니다.",
        "expiresIn", 600  // 10분
    ));
}
```

**Response**
```json
// 성공
{ "success": true, "message": "인증 코드가 발송되었습니다.", "expiresIn": 600 }

// 도메인 오류
{ "success": false, "code": "INVALID_EMAIL_DOMAIN", "message": "@koreatech.ac.kr 이메일만 사용 가능합니다." }

// 이미 가입됨
{ "success": false, "code": "EMAIL_EXISTS", "message": "이미 가입된 이메일입니다." }

// Rate Limit
{ "success": false, "code": "RATE_LIMITED", "message": "45초 후에 다시 시도해주세요.", "retryAfter": 45 }
```

---

### 6.4 이메일 인증코드 확인

**왜 인증 토큰을 발급하는가?**
- 인증 코드 확인 시점과 회원가입 시점이 다름
- 회원가입 시 이 이메일이 실제로 인증되었는지 서버가 확인해야 함
- 토큰 없이 이메일만 보내면 위조 가능 (2.1.1 참조)

**왜 시도 횟수를 제한하는가?**
- 6자리 숫자 코드는 100만 가지 경우의 수
- 무제한 시도 시 브루트 포스 공격 가능
- 5회 실패 시 코드 무효화하여 보호

**Request**
```http
POST /api/auth/verify-email
Content-Type: application/json

{ "email": "user@koreatech.ac.kr", "code": "123456" }
```

**Backend**
```java
@PostMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
    String email = request.getEmail();
    String code = request.getCode();
    
    String storedCode = redisTemplate.opsForValue().get("email:verify:" + email);
    
    // 1. 코드 없음 (만료됨)
    if (storedCode == null) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
            "success", false,
            "code", "CODE_EXPIRED",
            "message", "인증 코드가 만료되었습니다. 다시 발송해주세요."
        ));
    }
    
    // 2. 코드 불일치
    if (!storedCode.equals(code)) {
        // 시도 횟수 증가
        String attemptKey = "email:attempt:" + email;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, 5, TimeUnit.MINUTES);
        
        // 5회 초과 시 코드 무효화
        if (attempts >= 5) {
            redisTemplate.delete("email:verify:" + email);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false,
                "code", "TOO_MANY_ATTEMPTS",
                "message", "인증 시도 횟수를 초과했습니다. 다시 발송해주세요."
            ));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "code", "INVALID_CODE",
            "message", "인증 코드가 일치하지 않습니다.",
            "remainingAttempts", 5 - attempts
        ));
    }
    
    // 3. 성공 → 인증 코드 및 시도 횟수 삭제
    redisTemplate.delete("email:verify:" + email);
    redisTemplate.delete("email:attempt:" + email);
    
    // 4. 인증 토큰 발급 (30분 TTL)
    String token = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        "email:token:" + token, 
        email, 
        30, TimeUnit.MINUTES
    );
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "verified", true,
        "emailVerificationToken", token,
        "expiresIn", 1800,  // 30분
        "message", "이메일 인증이 완료되었습니다."
    ));
}
```

**Frontend 처리**
```typescript
const verifyEmailCode = async () => {
  setLoading(prev => ({ ...prev, isVerifyingEmail: true }))
  setError(prev => ({ ...prev, emailCode: null }))
  
  try {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/verify-email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: formData.email, code: emailCode }),
    })
    const data = await res.json()
    
    switch (res.status) {
      case 200:
        // 성공 → 토큰 저장
        setFormData(prev => ({ 
          ...prev, 
          emailVerificationToken: data.emailVerificationToken 
        }))
        setValidation(prev => ({ ...prev, isEmailVerified: true }))
        
        // 만료 시간 저장 (UI 경고용)
        setEmailTokenExpiresAt(Date.now() + data.expiresIn * 1000)
        break
        
      case 410:  // Gone - 코드 만료
        setError(prev => ({ ...prev, emailCode: data.message }))
        setEmailCodeSent(false)  // 재발송 필요
        break
        
      case 401:  // Unauthorized - 코드 불일치
        setError(prev => ({ 
          ...prev, 
          emailCode: `${data.message} (${data.remainingAttempts}회 남음)` 
        }))
        setEmailCode('')  // 입력 필드 초기화
        break
        
      case 429:  // Too Many Requests - 시도 횟수 초과
        setError(prev => ({ ...prev, emailCode: data.message }))
        setEmailCodeSent(false)  // 재발송 필요
        break
    }
  } catch (err) {
    setError(prev => ({ ...prev, emailCode: '서버 연결에 실패했습니다.' }))
  } finally {
    setLoading(prev => ({ ...prev, isVerifyingEmail: false }))
  }
}
```

---

### 6.5 GitHub 연동 (팝업 방식)

**왜 팝업 방식을 사용하는가?**

| 방식 | 장점 | 단점 |
|------|------|------|
| 리다이렉트 | 팝업 차단 없음 | 폼 데이터 소실, sessionStorage 의존 |
| **팝업** | 폼 데이터 유지, 메인 페이지 유지 | 팝업 차단 가능성 |

회원가입 중간에 OAuth를 진행하는 경우 **폼 데이터 보존**이 중요하므로 팝업 방식 선택.

**왜 GitHub 연동에도 토큰이 필요한가?**
- GitHub ID는 공개 정보 (누구나 알 수 있음)
- 토큰 없이 githubId만 보내면 타인의 GitHub로 가입 가능
- 자세한 공격 시나리오는 2.1.2 참조

#### 6.5.1 플로우

```
[부모 창: 회원가입 폼]
    │
    │ (1) "GitHub 연동" 버튼 클릭
    ▼
[팝업 창 열기]
    │ window.open('https://github.com/login/oauth/authorize?...')
    ▼
[GitHub 로그인 페이지]
    │
    │ (2) 사용자 로그인 & 권한 동의
    ▼
[GitHub → 팝업 콜백 URL로 리다이렉트]
    │ /signup/github-callback?code=xxx&state=xxx
    ▼
[팝업: 콜백 페이지]
    │ (3) state 검증 (CSRF 방지)
    │ (4) Backend로 code 전송
    │     POST /api/auth/github/exchange
    │ (5) Backend에서 GitHub 정보 + 토큰 반환
    │ (6) 부모 창으로 결과 전송 (postMessage)
    │ (7) 팝업 창 닫기
    ▼
[부모 창: message 이벤트 수신]
    │ (8) GitHub 정보 + 토큰 상태에 저장
    ▼
[회원가입 폼에 "✅ 연동 완료: @username" 표시]
```

#### 6.5.2 부모 창 - GitHub 연동 버튼

```typescript
// 부모 창에서 GitHub 연동 시작
const handleGitHubLink = () => {
  // 1. CSRF 방지용 state 생성
  const state = crypto.randomUUID()
  localStorage.setItem('github_oauth_state', state)
  
  // 2. GitHub OAuth URL 구성
  const params = new URLSearchParams({
    client_id: process.env.NEXT_PUBLIC_GITHUB_CLIENT_ID!,
    redirect_uri: `${window.location.origin}/signup/github-callback`,
    scope: 'read:user user:email',
    state: state,
  })
  
  // 3. 팝업 창 열기 (화면 중앙에)
  const width = 500, height = 700
  const left = window.screenX + (window.outerWidth - width) / 2
  const top = window.screenY + (window.outerHeight - height) / 2
  
  const popup = window.open(
    `https://github.com/login/oauth/authorize?${params}`,
    'github-oauth',
    `width=${width},height=${height},left=${left},top=${top}`
  )
  
  if (!popup) {
    // 팝업 차단됨
    setError(prev => ({ 
      ...prev, 
      github: '팝업이 차단되었습니다. 팝업 차단을 해제해주세요.' 
    }))
  }
}

// 팝업에서 메시지 수신
useEffect(() => {
  const handleMessage = (event: MessageEvent) => {
    // origin 검증 (보안)
    if (event.origin !== window.location.origin) return
    
    const { type, payload } = event.data
    
    if (type === 'GITHUB_AUTH_SUCCESS') {
      // GitHub 정보 + 토큰 저장
      setFormData(prev => ({
        ...prev,
        githubId: payload.githubId,
        githubUsername: payload.githubUsername,
        githubEmail: payload.githubEmail,
        githubVerificationToken: payload.githubVerificationToken,
      }))
      setValidation(prev => ({ ...prev, isGithubLinked: true }))
      
      // 만료 시간 저장 (UI 경고용)
      setGithubTokenExpiresAt(Date.now() + payload.expiresIn * 1000)
      
    } else if (type === 'GITHUB_AUTH_ERROR') {
      setError(prev => ({ ...prev, github: payload.message }))
    }
  }
  
  window.addEventListener('message', handleMessage)
  return () => window.removeEventListener('message', handleMessage)
}, [])
```

#### 6.5.3 팝업 콜백 페이지

```typescript
// app/(auth)/signup/github-callback/page.tsx

"use client"

import { useEffect, useState } from "react"
import { useSearchParams } from "next/navigation"

export default function GitHubCallbackPage() {
  const searchParams = useSearchParams()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  
  useEffect(() => {
    const handleCallback = async () => {
      const code = searchParams.get('code')
      const state = searchParams.get('state')
      const error = searchParams.get('error')
      
      // 1. 부모 창 확인
      if (!window.opener) {
        setStatus('error')
        setErrorMessage('잘못된 접근입니다.')
        return
      }
      
      // 2. GitHub 에러 처리
      if (error) {
        window.opener.postMessage({
          type: 'GITHUB_AUTH_ERROR',
          payload: { 
            message: error === 'access_denied' 
              ? 'GitHub 연동을 취소했습니다.' 
              : 'GitHub 인증에 실패했습니다.' 
          },
        }, window.location.origin)
        window.close()
        return
      }
      
      // 3. state 검증 (CSRF 방지)
      const savedState = localStorage.getItem('github_oauth_state')
      if (state !== savedState) {
        window.opener.postMessage({
          type: 'GITHUB_AUTH_ERROR',
          payload: { message: '보안 검증에 실패했습니다. 다시 시도해주세요.' },
        }, window.location.origin)
        window.close()
        return
      }
      localStorage.removeItem('github_oauth_state')
      
      // 4. Backend로 code 전송
      try {
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/auth/github/exchange`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code }),
          }
        )
        const data = await res.json()
        
        if (!res.ok) {
          window.opener.postMessage({
            type: 'GITHUB_AUTH_ERROR',
            payload: { message: data.message },
          }, window.location.origin)
          setStatus('error')
          setErrorMessage(data.message)
          return
        }
        
        // 5. 성공 → 부모에게 전달 (토큰 포함!)
        window.opener.postMessage({
          type: 'GITHUB_AUTH_SUCCESS',
          payload: {
            githubId: data.githubId,
            githubUsername: data.githubUsername,
            githubEmail: data.githubEmail,
            githubVerificationToken: data.githubVerificationToken,  // 토큰!
            expiresIn: data.expiresIn,
          },
        }, window.location.origin)
        
        setStatus('success')
        setTimeout(() => window.close(), 1000)
        
      } catch (err) {
        window.opener.postMessage({
          type: 'GITHUB_AUTH_ERROR',
          payload: { message: '서버 연결에 실패했습니다.' },
        }, window.location.origin)
        setStatus('error')
        setErrorMessage('서버 연결에 실패했습니다.')
      }
    }
    
    handleCallback()
  }, [searchParams])
  
  // UI 렌더링
  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4">
      {status === 'loading' && (
        <>
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900" />
          <p className="mt-4 text-gray-600">GitHub 연동 처리 중...</p>
        </>
      )}
      
      {status === 'success' && (
        <>
          <div className="text-green-500 text-5xl mb-4">✓</div>
          <p className="text-lg font-medium">GitHub 연동 완료!</p>
          <p className="text-gray-500 mt-2">창이 자동으로 닫힙니다...</p>
        </>
      )}
      
      {status === 'error' && (
        <>
          <div className="text-red-500 text-5xl mb-4">✕</div>
          <p className="text-lg font-medium text-red-600">{errorMessage}</p>
          <button
            onClick={() => window.close()}
            className="mt-4 px-4 py-2 bg-gray-800 text-white rounded hover:bg-gray-700"
          >
            닫기
          </button>
        </>
      )}
    </div>
  )
}
```

#### 6.5.4 Backend - GitHub code 교환

```java
@PostMapping("/github/exchange")
public ResponseEntity<?> exchangeGitHubCode(@RequestBody @Valid GitHubExchangeRequest request) {
    String code = request.getCode();
    
    try {
        // 1. GitHub에서 access_token 획득
        String accessToken = gitHubOAuthService.exchangeCodeForToken(code);
        
        // 2. GitHub 사용자 정보 조회
        GitHubUserInfo userInfo = gitHubOAuthService.fetchUserInfo(accessToken);
        
        // 3. 이미 연동된 계정인지 확인
        if (userRepository.existsByGithubId(userInfo.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "code", "GITHUB_ALREADY_LINKED",
                "message", "이미 다른 계정에 연동된 GitHub 계정입니다."
            ));
        }
        
        // 4. 인증 토큰 발급 (30분 TTL) ★ 핵심!
        String token = UUID.randomUUID().toString();
        
        // GitHub 정보를 JSON으로 Redis에 저장
        String githubData = objectMapper.writeValueAsString(Map.of(
            "githubId", userInfo.getId(),
            "githubUsername", userInfo.getLogin(),
            "githubEmail", userInfo.getEmail() != null ? userInfo.getEmail() : ""
        ));
        
        redisTemplate.opsForValue().set(
            "github:token:" + token,
            githubData,
            30, TimeUnit.MINUTES
        );
        
        // 5. 응답 (토큰 포함)
        return ResponseEntity.ok(Map.of(
            "success", true,
            "githubId", userInfo.getId(),
            "githubUsername", userInfo.getLogin(),
            "githubEmail", userInfo.getEmail(),
            "githubVerificationToken", token,  // 토큰!
            "expiresIn", 1800  // 30분
        ));
        
    } catch (GitHubOAuthException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "code", "GITHUB_AUTH_FAILED",
            "message", "GitHub 인증에 실패했습니다."
        ));
    }
}
```

---

### 6.6 회원가입

**서버에서 수행하는 검증 목록:**

| 순서 | 검증 항목 | 실패 시 |
|------|----------|---------|
| 1 | 이용약관 동의 여부 | 400 - 이용약관에 동의해주세요 |
| 2 | 개인정보처리방침 동의 여부 | 400 - 개인정보처리방침에 동의해주세요 |
| 3 | 아이디 형식 | 400 - 아이디는 4-20자의 영문, 숫자만 사용 가능합니다 |
| 4 | 아이디 중복 | 409 - 이미 사용중인 아이디입니다 |
| 5 | 비밀번호 형식 | 400 - 비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다 |
| 6 | 학번/사번 형식 | 400 - 형식 오류 메시지 |
| 7 | 학번/사번 중복 | 409 - 이미 가입된 학번/사번입니다 |
| 8 | 이름 형식 | 400 - 이름은 2자 이상이어야 합니다 |
| 9 | 이메일 도메인 | 400 - @koreatech.ac.kr 이메일만 사용 가능합니다 |
| 10 | 이메일 중복 | 409 - 이미 가입된 이메일입니다 |
| 11 | **이메일 인증 토큰 유효성** | 410 - 이메일 인증이 만료되었습니다 |
| 12 | **이메일 인증 토큰과 이메일 일치** | 400 - 이메일 인증 정보가 일치하지 않습니다 |
| 13 | **GitHub 연동 토큰 유효성** | 410 - GitHub 연동이 만료되었습니다 |
| 14 | **GitHub 연동 토큰과 githubId 일치** | 400 - GitHub 연동 정보가 일치하지 않습니다 |
| 15 | GitHub ID 중복 | 409 - 이미 다른 계정에 연동된 GitHub 계정입니다 |

**Request**
```http
POST /api/auth/signup
Content-Type: application/json

{
  "memberType": "STUDENT",
  "username": "testuser",
  "password": "Test1234!",
  "memberId": "2024136000",
  "name": "홍길동",
  "email": "user@koreatech.ac.kr",
  "emailVerificationToken": "550e8400-e29b-41d4-a716-446655440000",
  "githubId": "12345678",
  "githubUsername": "octocat",
  "githubEmail": "octocat@github.com",
  "githubVerificationToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "termsAgreed": true,
  "privacyAgreed": true
}
```

**Backend**
```java
@PostMapping("/signup")
@Transactional
public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
    
    // ===== 1. 약관 동의 검증 =====
    if (!request.getTermsAgreed()) {
        return badRequest("TERMS_NOT_AGREED", "termsAgreed", "이용약관에 동의해주세요.");
    }
    if (!request.getPrivacyAgreed()) {
        return badRequest("PRIVACY_NOT_AGREED", "privacyAgreed", "개인정보처리방침에 동의해주세요.");
    }
    
    // ===== 2. 아이디 검증 =====
    String username = request.getUsername();
    if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
        return badRequest("INVALID_USERNAME", "username", "아이디는 4-20자의 영문, 숫자만 사용 가능합니다.");
    }
    if (userRepository.existsByUsername(username)) {
        return conflict("USERNAME_EXISTS", "username", "이미 사용중인 아이디입니다.");
    }
    
    // ===== 3. 비밀번호 검증 =====
    String password = request.getPassword();
    if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
        return badRequest("INVALID_PASSWORD", "password", "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다.");
    }
    
    // ===== 4. 회원유형 & 학번/사번 검증 =====
    MemberType memberType = request.getMemberType();
    String memberId = request.getMemberId();
    
    if (memberType == MemberType.STUDENT) {
        if (!memberId.matches("^\\d{10}$")) {
            return badRequest("INVALID_STUDENT_ID", "memberId", "학번은 10자리 숫자여야 합니다.");
        }
    } else {
        if (!memberId.matches("^(\\d{6}|\\d{8})$")) {
            return badRequest("INVALID_STAFF_ID", "memberId", "사번은 6자리 또는 8자리 숫자여야 합니다.");
        }
    }
    if (userRepository.existsByMemberTypeAndMemberId(memberType, memberId)) {
        String label = memberType == MemberType.STUDENT ? "학번" : "사번";
        return conflict("MEMBER_ID_EXISTS", "memberId", "이미 가입된 " + label + "입니다.");
    }
    
    // ===== 5. 이름 검증 =====
    if (request.getName() == null || request.getName().trim().length() < 2) {
        return badRequest("INVALID_NAME", "name", "이름은 2자 이상이어야 합니다.");
    }
    
    // ===== 6. 이메일 검증 =====
    String email = request.getEmail();
    if (!email.endsWith("@koreatech.ac.kr")) {
        return badRequest("INVALID_EMAIL_DOMAIN", "email", "@koreatech.ac.kr 이메일만 사용 가능합니다.");
    }
    if (userRepository.existsByEmail(email)) {
        return conflict("EMAIL_EXISTS", "email", "이미 가입된 이메일입니다.");
    }
    
    // ===== 7. 이메일 인증 토큰 검증 ★ =====
    String emailToken = request.getEmailVerificationToken();
    String verifiedEmail = redisTemplate.opsForValue().get("email:token:" + emailToken);
    
    // 토큰 만료됨
    if (verifiedEmail == null) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
            "success", false,
            "code", "EMAIL_TOKEN_EXPIRED",
            "field", "email",
            "message", "이메일 인증이 만료되었습니다. 다시 인증해주세요."
        ));
    }
    
    // 토큰의 이메일과 요청 이메일 불일치
    if (!verifiedEmail.equals(email)) {
        return badRequest("EMAIL_TOKEN_MISMATCH", "email", "이메일 인증 정보가 일치하지 않습니다.");
    }
    
    // ===== 8. GitHub 연동 토큰 검증 ★ =====
    String githubToken = request.getGithubVerificationToken();
    String storedGithubData = redisTemplate.opsForValue().get("github:token:" + githubToken);
    
    // 토큰 만료됨
    if (storedGithubData == null) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
            "success", false,
            "code", "GITHUB_TOKEN_EXPIRED",
            "field", "github",
            "message", "GitHub 연동이 만료되었습니다. 다시 연동해주세요."
        ));
    }
    
    // 저장된 GitHub 정보 파싱
    GitHubData githubData = objectMapper.readValue(storedGithubData, GitHubData.class);
    
    // 토큰의 githubId와 요청 githubId 불일치
    if (!githubData.getGithubId().equals(request.getGithubId())) {
        return badRequest("GITHUB_TOKEN_MISMATCH", "github", "GitHub 연동 정보가 일치하지 않습니다.");
    }
    
    // ===== 9. GitHub ID 중복 확인 =====
    if (userRepository.existsByGithubId(request.getGithubId())) {
        return conflict("GITHUB_ALREADY_LINKED", "github", "이미 다른 계정에 연동된 GitHub 계정입니다.");
    }
    
    // ===== 10. 사용자 생성 =====
    LocalDateTime now = LocalDateTime.now();
    
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setMemberType(memberType);
    user.setMemberId(memberId);
    user.setName(request.getName().trim());
    user.setEmail(email);
    user.setGithubId(githubData.getGithubId());
    user.setGithubUsername(githubData.getGithubUsername());
    user.setGithubEmail(githubData.getGithubEmail());
    user.setTermsAgreed(true);
    user.setPrivacyAgreed(true);
    user.setTermsAgreedAt(now);
    user.setPrivacyAgreedAt(now);
    user.setStatus(UserStatus.ACTIVE);
    
    try {
        userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
        // 동시 가입 시도로 인한 unique constraint 위반
        return conflict("REGISTRATION_CONFLICT", null, "회원가입에 실패했습니다. 다시 시도해주세요.");
    }
    
    // ===== 11. 사용된 토큰 삭제 =====
    redisTemplate.delete("email:token:" + emailToken);
    redisTemplate.delete("github:token:" + githubToken);
    
    // ===== 12. JWT 발급 =====
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
    
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
        "success", true,
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "expiresIn", 1800,
        "user", UserDto.from(user),
        "message", "회원가입이 완료되었습니다."
    ));
}

// Helper 메서드
private ResponseEntity<?> badRequest(String code, String field, String message) {
    return ResponseEntity.badRequest().body(Map.of(
        "success", false,
        "code", code,
        "field", field,
        "message", message
    ));
}

private ResponseEntity<?> conflict(String code, String field, String message) {
    Map<String, Object> body = new HashMap<>();
    body.put("success", false);
    body.put("code", code);
    body.put("message", message);
    if (field != null) body.put("field", field);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}
```

**Frontend 처리**
```typescript
const handleSubmit = async () => {
  setLoading(prev => ({ ...prev, isSubmitting: true }))
  setError(prev => ({ ...prev, submit: null }))
  
  try {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        memberType: formData.memberType,
        username: formData.username,
        password: formData.password,
        memberId: formData.memberId,
        name: formData.name,
        email: formData.email,
        emailVerificationToken: formData.emailVerificationToken,
        githubId: formData.githubId,
        githubUsername: formData.githubUsername,
        githubEmail: formData.githubEmail,
        githubVerificationToken: formData.githubVerificationToken,
        termsAgreed: formData.termsAgreed,
        privacyAgreed: formData.privacyAgreed,
      }),
    })
    const data = await res.json()
    
    if (res.status === 201) {
      // 성공 → 자동 로그인
      await signIn('credentials', {
        username: formData.username,
        password: formData.password,
        redirect: false,
      })
      router.push('/')
      return
    }
    
    // 에러 처리
    handleSignupError(res.status, data)
    
  } catch (err) {
    setError(prev => ({ ...prev, submit: '서버 연결에 실패했습니다.' }))
  } finally {
    setLoading(prev => ({ ...prev, isSubmitting: false }))
  }
}

const handleSignupError = (status: number, data: any) => {
  const { code, field, message } = data
  
  // 필드별 에러 처리
  if (field) {
    switch (field) {
      case 'username':
        setError(prev => ({ ...prev, username: message }))
        setValidation(prev => ({ ...prev, isUsernameAvailable: false }))
        break
        
      case 'memberId':
        setError(prev => ({ ...prev, memberId: message }))
        setValidation(prev => ({ ...prev, isMemberIdAvailable: false }))
        break
        
      case 'email':
        setError(prev => ({ ...prev, email: message }))
        // 토큰 만료 시 재인증 필요
        if (code === 'EMAIL_TOKEN_EXPIRED') {
          setValidation(prev => ({ ...prev, isEmailVerified: false }))
          setFormData(prev => ({ ...prev, emailVerificationToken: null }))
        }
        break
        
      case 'github':
        setError(prev => ({ ...prev, github: message }))
        // 토큰 만료 시 재연동 필요
        if (code === 'GITHUB_TOKEN_EXPIRED') {
          setValidation(prev => ({ ...prev, isGithubLinked: false }))
          setFormData(prev => ({ 
            ...prev, 
            githubId: null, 
            githubUsername: null,
            githubVerificationToken: null 
          }))
        }
        break
        
      default:
        setError(prev => ({ ...prev, submit: message }))
    }
    
    // 해당 필드로 스크롤
    document.getElementById(`${field}-section`)?.scrollIntoView({ behavior: 'smooth' })
    
  } else {
    setError(prev => ({ ...prev, submit: message }))
  }
}
```

---

### 6.7 로그인 (아이디/비밀번호)

**왜 에러 메시지를 구분하지 않는가?**
- "아이디가 존재하지 않습니다" → 공격자가 유효한 아이디 목록 수집 가능
- "비밀번호가 틀렸습니다" → 해당 아이디가 존재함을 알려줌
- 따라서 항상 "아이디 또는 비밀번호가 올바르지 않습니다"로 통일

**Request**
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "testuser", "password": "Test1234!" }
```

**Backend**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
    String username = request.getUsername();
    String password = request.getPassword();
    
    // 1. 사용자 조회
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
        // 아이디가 없어도 같은 메시지
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "code", "INVALID_CREDENTIALS",
            "message", "아이디 또는 비밀번호가 올바르지 않습니다."
        ));
    }
    
    User user = userOpt.get();
    
    // 2. 비밀번호 검증 (BCrypt)
    if (!passwordEncoder.matches(password, user.getPassword())) {
        // 비밀번호가 틀려도 같은 메시지
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "code", "INVALID_CREDENTIALS",
            "message", "아이디 또는 비밀번호가 올바르지 않습니다."
        ));
    }
    
    // 3. 계정 상태 확인
    if (user.getStatus() == UserStatus.SUSPENDED) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "code", "ACCOUNT_SUSPENDED",
            "message", "정지된 계정입니다. 관리자에게 문의하세요."
        ));
    }
    
    if (user.getStatus() == UserStatus.INACTIVE) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "code", "ACCOUNT_INACTIVE",
            "message", "비활성화된 계정입니다."
        ));
    }
    
    // 4. 로그인 시간 업데이트
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);
    
    // 5. JWT 발급
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "expiresIn", 1800,
        "user", UserDto.from(user)
    ));
}
```

---

### 6.8 로그인 (GitHub)

**Request**
```http
POST /api/auth/github-login
Content-Type: application/json

{ "githubId": "12345678", "githubUsername": "octocat" }
```

**Backend**
```java
@PostMapping("/github-login")
public ResponseEntity<?> githubLogin(@RequestBody @Valid GitHubLoginRequest request) {
    String githubId = request.getGithubId();
    
    // 1. GitHub ID로 사용자 조회
    Optional<User> userOpt = userRepository.findByGithubId(githubId);
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "success", false,
            "code", "GITHUB_NOT_LINKED",
            "message", "연동된 계정이 없습니다. 회원가입을 진행해주세요."
        ));
    }
    
    User user = userOpt.get();
    
    // 2. 계정 상태 확인
    if (user.getStatus() == UserStatus.SUSPENDED) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "code", "ACCOUNT_SUSPENDED",
            "message", "정지된 계정입니다."
        ));
    }
    
    // 3. 로그인 시간 업데이트 & JWT 발급
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);
    
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "expiresIn", 1800,
        "user", UserDto.from(user)
    ));
}
```

---

## 7. NextAuth 설정

```typescript
// auth.ts

import NextAuth from "next-auth"
import Credentials from "next-auth/providers/credentials"
import GitHub from "next-auth/providers/github"

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    // 아이디/비밀번호 로그인
    Credentials({
      credentials: {
        username: { label: "아이디", type: "text" },
        password: { label: "비밀번호", type: "password" },
      },
      async authorize(credentials) {
        try {
          const res = await fetch(`${process.env.API_URL}/api/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              username: credentials?.username,
              password: credentials?.password,
            }),
          })
          
          const data = await res.json()
          
          if (!res.ok) {
            throw new Error(data.message)
          }
          
          return {
            id: String(data.user.id),
            name: data.user.name,
            email: data.user.email,
            username: data.user.username,
            memberType: data.user.memberType,
            memberId: data.user.memberId,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
          }
        } catch (error) {
          throw error
        }
      },
    }),
    
    // GitHub 로그인
    GitHub({
      clientId: process.env.GITHUB_ID!,
      clientSecret: process.env.GITHUB_SECRET!,
    }),
  ],
  
  callbacks: {
    async signIn({ user, account, profile }) {
      // GitHub 로그인인 경우
      if (account?.provider === "github") {
        try {
          const res = await fetch(`${process.env.API_URL}/api/auth/github-login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              githubId: String(profile?.id),
              githubUsername: profile?.login,
            }),
          })
          
          const data = await res.json()
          
          if (res.status === 404) {
            // 회원가입 필요 → 회원가입 페이지로 리다이렉트
            const params = new URLSearchParams({
              github_id: String(profile?.id),
              github_username: profile?.login as string,
              github_email: profile?.email || "",
            })
            return `/signup?${params.toString()}`
          }
          
          if (!res.ok) {
            return `/login?error=${encodeURIComponent(data.message)}`
          }
          
          // 성공 → user에 토큰 저장
          user.id = String(data.user.id)
          user.accessToken = data.accessToken
          user.refreshToken = data.refreshToken
          user.memberType = data.user.memberType
          user.memberId = data.user.memberId
          user.username = data.user.username
          
          return true
        } catch (error) {
          return `/login?error=${encodeURIComponent("서버 연결에 실패했습니다.")}`
        }
      }
      
      return true
    },
    
    async jwt({ token, user, trigger, session }) {
      if (user) {
        token.accessToken = user.accessToken
        token.refreshToken = user.refreshToken
        token.userId = user.id
        token.username = user.username
        token.memberType = user.memberType
        token.memberId = user.memberId
      }
      
      // 세션 업데이트 (토큰 갱신 시)
      if (trigger === "update" && session) {
        token.accessToken = session.accessToken
        token.refreshToken = session.refreshToken
      }
      
      return token
    },
    
    async session({ session, token }) {
      session.accessToken = token.accessToken as string
      session.refreshToken = token.refreshToken as string
      session.user.id = token.userId as string
      session.user.username = token.username as string
      session.user.memberType = token.memberType as string
      session.user.memberId = token.memberId as string
      return session
    },
  },
  
  pages: {
    signIn: "/login",
    error: "/login",
  },
})
```

---

## 8. 타입 정의

### 8.1 Frontend

```typescript
// types/auth.ts

export type MemberType = 'STUDENT' | 'STAFF'

export interface SignupFormData {
  memberType: MemberType | null
  username: string
  password: string
  passwordConfirm: string
  memberId: string
  name: string
  email: string
  emailVerificationToken: string | null
  githubId: string | null
  githubUsername: string | null
  githubEmail: string | null
  githubVerificationToken: string | null
  termsAgreed: boolean
  privacyAgreed: boolean
}

export interface ValidationState {
  isUsernameChecked: boolean
  isUsernameAvailable: boolean
  isPasswordValid: boolean
  isPasswordMatch: boolean
  isMemberIdChecked: boolean
  isMemberIdAvailable: boolean
  isEmailVerified: boolean
  isGithubLinked: boolean
}

export interface LoadingState {
  isCheckingUsername: boolean
  isCheckingMemberId: boolean
  isSendingEmail: boolean
  isVerifyingEmail: boolean
  isSubmitting: boolean
}

export interface ErrorState {
  username: string | null
  password: string | null
  passwordConfirm: string | null
  memberId: string | null
  name: string | null
  email: string | null
  emailCode: string | null
  github: string | null
  submit: string | null
}

// NextAuth 타입 확장
declare module "next-auth" {
  interface User {
    username?: string
    accessToken?: string
    refreshToken?: string
    memberType?: string
    memberId?: string
  }
  
  interface Session {
    accessToken: string
    refreshToken: string
    user: {
      id: string
      name: string
      email: string
      username: string
      memberType: string
      memberId: string
    }
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string
    refreshToken?: string
    userId?: string
    username?: string
    memberType?: string
    memberId?: string
  }
}
```

### 8.2 Backend DTO

```java
// SignupRequest.java
@Data
public class SignupRequest {
    @NotNull
    private MemberType memberType;
    
    @NotBlank
    @Size(min = 4, max = 20)
    private String username;
    
    @NotBlank
    @Size(min = 8)
    private String password;
    
    @NotBlank
    private String memberId;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String emailVerificationToken;
    
    @NotBlank
    private String githubId;
    
    @NotBlank
    private String githubUsername;
    
    private String githubEmail;
    
    @NotBlank
    private String githubVerificationToken;
    
    @NotNull
    private Boolean termsAgreed;
    
    @NotNull
    private Boolean privacyAgreed;
}

// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}

// GitHubData.java (Redis 저장용)
@Data
public class GitHubData {
    private String githubId;
    private String githubUsername;
    private String githubEmail;
}
```

---

## 9. API 요약

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/auth/check-username?username=xxx` | 아이디 중복확인 |
| GET | `/api/auth/check-member-id?type=xxx&id=xxx` | 학번/사번 중복확인 |
| POST | `/api/auth/send-email` | 이메일 인증코드 발송 |
| POST | `/api/auth/verify-email` | 이메일 인증코드 확인 → **토큰 발급** |
| POST | `/api/auth/github/exchange` | GitHub code 교환 → **토큰 발급** |
| POST | `/api/auth/signup` | 회원가입 (**토큰 검증**) |
| POST | `/api/auth/login` | 아이디/비밀번호 로그인 |
| POST | `/api/auth/github-login` | GitHub 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |

---

## 10. 에러 코드 정리

| 코드 | HTTP | 필드 | 설명 |
|------|------|------|------|
| `TERMS_NOT_AGREED` | 400 | termsAgreed | 이용약관 미동의 |
| `PRIVACY_NOT_AGREED` | 400 | privacyAgreed | 개인정보처리방침 미동의 |
| `INVALID_USERNAME` | 400 | username | 아이디 형식 오류 |
| `USERNAME_EXISTS` | 409 | username | 아이디 중복 |
| `INVALID_PASSWORD` | 400 | password | 비밀번호 형식 오류 |
| `INVALID_STUDENT_ID` | 400 | memberId | 학번 형식 오류 |
| `INVALID_STAFF_ID` | 400 | memberId | 사번 형식 오류 |
| `MEMBER_ID_EXISTS` | 409 | memberId | 학번/사번 중복 |
| `INVALID_NAME` | 400 | name | 이름 형식 오류 |
| `INVALID_EMAIL_DOMAIN` | 400 | email | 이메일 도메인 오류 |
| `EMAIL_EXISTS` | 409 | email | 이메일 중복 |
| `CODE_EXPIRED` | 410 | email | 인증 코드 만료 |
| `INVALID_CODE` | 401 | email | 인증 코드 불일치 |
| `TOO_MANY_ATTEMPTS` | 429 | email | 인증 시도 초과 |
| `EMAIL_TOKEN_EXPIRED` | 410 | email | 이메일 인증 토큰 만료 |
| `EMAIL_TOKEN_MISMATCH` | 400 | email | 이메일 인증 정보 불일치 |
| `GITHUB_NOT_LINKED` | 400/404 | github | GitHub 미연동 |
| `GITHUB_ALREADY_LINKED` | 409 | github | GitHub 이미 연동 |
| `GITHUB_AUTH_FAILED` | 400 | github | GitHub 인증 실패 |
| `GITHUB_TOKEN_EXPIRED` | 410 | github | GitHub 연동 토큰 만료 |
| `GITHUB_TOKEN_MISMATCH` | 400 | github | GitHub 연동 정보 불일치 |
| `INVALID_CREDENTIALS` | 401 | - | 아이디/비밀번호 오류 |
| `ACCOUNT_SUSPENDED` | 403 | - | 계정 정지 |
| `ACCOUNT_INACTIVE` | 403 | - | 계정 비활성화 |
| `RATE_LIMITED` | 429 | - | 요청 제한 초과 |

---

## 11. 보안 체크리스트

| 항목 | 구현 | 이유 |
|------|------|------|
| 비밀번호 암호화 | BCrypt | 단방향 해시, 레인보우 테이블 방어 |
| 비밀번호 정책 | 8자+, 영문+숫자+특수문자 | 브루트 포스 방어 |
| 이메일 도메인 제한 | @koreatech.ac.kr만 | 학교 구성원만 가입 |
| 이메일 인증 토큰 | Redis 30분 TTL | 이메일 소유 증명 |
| GitHub 연동 토큰 | Redis 30분 TTL | GitHub 소유 증명 |
| CSRF 방지 | OAuth state 파라미터 | 크로스 사이트 요청 위조 방지 |
| Rate Limiting | 이메일 1회/분, 인증 5회 | 서비스 남용 방지 |
| JWT 보안 | Access 30분, Refresh 7일 | 토큰 탈취 피해 최소화 |
| Refresh Token Rotation | 갱신 시 새 토큰 발급 | 토큰 재사용 방지 |
| 로그인 실패 메시지 | 아이디/비밀번호 구분 안함 | 계정 열거 공격 방지 |
| 중복 방지 | DB unique constraint | Race Condition 대응 |
| 약관 동의 기록 | 시간 저장 | 법적 증빙 |

---

## 12. 환경 변수

### Frontend (.env.local)
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_GITHUB_CLIENT_ID=xxx

NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=xxx

GITHUB_ID=xxx
GITHUB_SECRET=xxx
```

### Backend (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yourdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 1800000   # 30분
  refresh-token-validity: 604800000  # 7일

github:
  client-id: ${GITHUB_CLIENT_ID}
  client-secret: ${GITHUB_CLIENT_SECRET}

mail:
  host: smtp.gmail.com
  port: 587
  username: ${MAIL_USERNAME}
  password: ${MAIL_PASSWORD}
```

---

## 13. 구현 체크리스트

### Frontend

- [ ] 회원가입 폼 컴포넌트
  - [ ] 회원 유형 선택 (학생/교직원)
  - [ ] 약관 동의 체크박스
  - [ ] 아이디 입력 + 중복확인
  - [ ] 비밀번호 입력 + 실시간 검증
  - [ ] 비밀번호 확인 + 일치 검증
  - [ ] 학번/사번 입력 + 중복확인
  - [ ] 이름 입력
  - [ ] 이메일 입력 + 인증코드 발송/확인
  - [ ] GitHub 연동 (팝업)
  - [ ] 회원가입 버튼 활성화 조건
- [ ] GitHub 콜백 페이지
- [ ] 로그인 폼
- [ ] NextAuth 설정
- [ ] 타입 정의
- [ ] 에러 핸들링

### Backend

- [ ] User Entity
- [ ] Repository
- [ ] AuthController
- [ ] AuthService
- [ ] EmailService
- [ ] GitHubOAuthService
- [ ] JwtTokenProvider
- [ ] JwtAuthenticationFilter
- [ ] SecurityConfig
- [ ] RedisConfig
- [ ] DTO 클래스들
- [ ] Exception Handler
