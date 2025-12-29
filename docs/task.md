# KOSP Task List (Detailed)

본 문서는 `기능명세서 (최종) - 기능명세서.csv`의 모든 기능을 포함하며, 구현 상태와 에러 핸들링 검증 여부를 나타냅니다.

**기호 설명**:
- ✅ 완료: 기능 구현 + 예외 처리(4xx/5xx) 검증 완료
- ⚠️ 부분: 기능 구현됨 but 예외 처리 미흡 or 관리자 기능 누락
- ⏳ 대기: 미구현 (우선순위 낮음)
- 🧪 예정: 미구현 (Priority 3/4 진행 예정)

---

## 1. 로그인 (SGI)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **SGI-001** | 이메일 | 이메일 로그인 | ✅ 완료 | `AuthController` (401/400 처리됨) |
| **SGI-002** | 깃허브 | 소셜 로그인 | ✅ 완료 | `OAuth2Controller` |

## 2. 회원가입 (SGU)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **SGU-001** | 재학생 인증 | 아우누리 이메일 인증 | ⏳ 대기 | (팀원 담당) MailSender 미구현 |
| **SGU-002** | 계정정보 | 회원가입 처리 | ✅ 완료 | `UserService` (이메일 중복 체크 포함) |
| **SGU-003** | 권한 | 기본 권한(학생) 할당 | ✅ 완료 | `UserService` (Fix & Refactor: 재가입 복구 로직 포함) |

## 3. 회원정보 (USR)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **USR-001** | 비밀번호 | 비밀번호 변경 | ⏳ 대기 | 이메일 발송 선행 필요 |
| **USR-007** | 정보 | 사용자 정보 수정 | ✅ 완료 | `UserController.update` |
| **USR-002** | 탈퇴 | 회원 탈퇴 | ✅ 완료 | `UserController.delete` (`/users/{id}` Spec 일치화) |
| **USR-003** | 활동 | 깃허브 활동 내역 조회 | ✅ 완료 | **Spring Batch** + `getGithubActivities` API 구현 (Repo List) |
| **USR-004** | 활동 | 작성한 글 조회 | ✅ 완료 | `UserActivityController` (빈 리스트 반환 확인) |
| **USR-005** | 활동 | 작성한 댓글 조회 | ✅ 완료 | `UserActivityController` |
| **USR-006** | 활동 | 즐겨찾기 조회 | ✅ 완료 | `UserActivityController` |

## 4. 챌린지 (CLG)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **CLG-001** | 도전과제 | 깃허브 활동내역 조회/평가 | ✅ 완료 | **SpEL** 기반 평가 로직 구현 완료 |
| **CLG-002** | 도전과제 | 챌린지 목록 조회 | ✅ 완료 | `ChallengeController.getChallenges` (`/v1/challenges`) |

## 5. 커뮤니티 (TAK)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **TAK-001** | 게시글 | 게시글 작성 | ✅ 완료 | `@Valid` + 권한 체크 완료 |
| **TAK-002** | 게시글 | 댓글 작성 | ✅ 완료 | `CommentController` |
| **TAK-003** | 게시글 | 즐겨찾기 | ✅ 완료 | `ArticleController` |
| **TAK-004** | 게시글 | 신고 | ✅ 완료 | `ReportController` (중복 방지, 댓글 신고 추가 필요) |
| **TAK-005** | 게시글 | 좋아요 | ✅ 완료 | `ArticleController` |
| **TAK-006** | 게시글 | 게시글 조회 | ✅ 완료 | `ArticleController` (404 처리) |

## 6. 팀게시판 (TBD)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **TBD-001** | 모집공고 | 공고 조회 | ✅ 완료 | `RecruitController` |
| **TBD-002** | 모집공고 | 공고 작성 | ✅ 완료 | `RecruitController` |
| **TBD-003** | 모집공고 | 팀 별 모집공고 조회 | ✅ 완료 | `TeamController` |
| **TBD-004** | 모집공고 | 공고 상세 조회 | ✅ 완료 | `RecruitController.getDetail` |
| **TBD-005** | 모집공고 | 공고 게시글 댓글 작성 | ✅ 완료 | `Community` 댓글과 통합됨 |
| **TBD-006** | 모집공고 | 공고 지원 | ✅ 완료 | `RecruitApplyService` (중복 지원 체크 완료) |

## 7. 관리자 (ADM)
| 기능 ID | 소분류 | 기능명 | 상태 | 구현 상세 / 비고 |
|:---:|:---:|:---:|:---:|:---|
| **ADM-001** | 사용자 | 사용자 목록 조회 | ✅ 완료 | `AdminController.getAllUsers` |
| **ADM-001** | 사용자 | 사용자 상세 조회 | ✅ 완료 | `AdminController.getUserDetail` |
| **ADM-001** | 사용자 | 사용자 정보 변경 (관리자) | ✅ 완료 | `AdminController.updateUser` (강제 수정) |
| **ADM-002** | 사용자 | 사용자 강제 탈퇴 | ✅ 완료 | `AdminController.deleteUser` |
| **ADM-003** | 챌린지 | 챌린지 생성 | ✅ 완료 | `AdminController.createChallenge` (SpEL 검증 포함) |
| **ADM-004** | 챌린지 | 챌린지 목록/상세 조회 | ✅ 완료 | `ChallengeController` (사용자/관리자 공용) |
| **ADM-005** | 챌린지 | 챌린지 수정 | ✅ 완료 | `AdminController.updateChallenge` |
| **ADM-006** | 챌린지 | 챌린지 삭제 | ✅ 완료 | `AdminController.deleteChallenge` |
| **ADM-007** | 게시글 | 공지 작성 | ✅ 완료 | `AdminController` (`isPinned`) |
| **ADM-008** | 게시글 | 공지 삭제 | ✅ 완료 | `AdminController` |
| **ADM-009** | 신고 | 신고 처리 | ✅ 완료 | `AdminController` (접수 목록/처리) |
| **ADM-010** | 점검 | 접근 제한 | ✅ 완료 | `PermissionAspect` (AOP) |
| **ADM-011** | 정책 | 정책 관리 | ✅ 완료 | 정책 생성/수정/삭제 (`PolicyAdminService`) |
| **ADM-012** | 조회 | 통합 검색 | ✅ 완료 | 사용자/게시글/댓글 등 통합 검색 기능 |

---

## Next Action Plan

**Priority 3: Complex Logic (진행 중)**
1.  **챌린지 데이터 초기화**: `ChallengeInitializer`를 통해 기본 티어(브론즈/실버/골드) 자동 생성.

**Priority 4: Infrastructure / AI**
1.  **신고 기능 고도화 (TAK-004)**: 중복 신고 방지 및 댓글 신고 기능 구현.
2.  **On-Demand Sync (n8n)**: 외부 시스템(n8n) 트리거를 통한 GitHub 활동 실시간 동기화.

## 🏗️ 미구현 컴포넌트 현황 (기능별 → Layer별)

### 1. 이메일 인증 & 비밀번호 (SGU-001, USR-001)
| Layer | Component | Status | Note |
|:---:|:---|:---:|:---|
| **Domain** | `EmailVerification` | ❌ 미구현 | 인증 코드 관리 (Redis 권장) |
| **Service** | `MailService` | ❌ 미구현 | `JavaMailSender` 연동 메일 발송 |
| **Controller** | `UserController` | ❌ 미구현 | 비밀번호 변경 API (`POST /users/password`) |

### 3. 시스템 초기화
| Layer | Component | Status | Note |
|:---:|:---|:---:|:---|
| **Initializer** | `ChallengeInitializer` | ⚠️ 부분 | 기본 챌린지 데이터 적재 로직 비어있음 |
