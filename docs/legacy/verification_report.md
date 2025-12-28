# 기능 검증 보고서 (Verification Report)

본 보고서는 `기능명세서 (최종) - 기능명세서.csv`와 현재 구현된 소스 코드(`Controller` 및 `api_spec.md`)를 비교하여 구현 상태를 점검한 결과입니다.

## 1. 구현된 기능 (Implemented Features)

현재 소스 코드 상에 Controller 및 Service 로직이 존재하여 정상 동작할 것으로 예상되는 기능입니다.

### 1.1 인증 및 계정 (Auth & User)
| 기능 ID | 기능명 | 상태 | 구현 코드 | 비고 |
|:---:|:---:|:---:|:---:|:---|
| **SGI-001** | 이메일 로그인 | ✅ 완료 | `AuthController.login` | DB 기반 로그인 |
| **SGI-002** | 소셜 로그인 | ✅ 완료 | `OAuth2Controller` | GitHub 로그인 |
| **SGU-002** | 회원가입 처리 | ✅ 완료 (복구됨) | `UserController.signup` | `UserApi` 복구 완료 |
| **USR-004** | 작성한 글 조회 | ✅ 완료 | `UserActivityController.getPosts` | |
| **USR-005** | 작성한 댓글 조회 | ✅ 완료 | `UserActivityController.getComments` | |
| **USR-006** | 즐겨찾기 조회 | ✅ 완료 | `UserActivityController.getBookmarks` | |
| **ADM-001** | 사용자 정보 변경 | ⚠️ 부분 완료 | `UserController.update` | 본인 정보 수정만 구현됨 (관리자 기능은 미구현) |

### 1.2 커뮤니티 (Community)
| 기능 ID | 기능명 | 상태 | 구현 코드 | 비고 |
|:---:|:---:|:---:|:---:|:---|
| **TAK-001** | 게시글 작성 | ✅ 완료 | `ArticleController.create` | |
| **TAK-002** | 댓글 작성 | ✅ 완료 | `CommentController.create` | 게시글/모집공고 통합 지원 |
| **TAK-003** | 즐겨찾기 (북마크) | ✅ 완료 | `ArticleController.toggleBookmark` | |
| **TAK-005** | 좋아요 | ✅ 완료 | `ArticleController.toggleLike` | |
| **TAK-006** | 게시글 조회 | ✅ 완료 | `ArticleController.getDetail` | |

### 1.3 모집 및 팀 (Recruit & Team)
| 기능 ID | 기능명 | 상태 | 구현 코드 | 비고 |
|:---:|:---:|:---:|:---:|:---|
| **TBD-001** | 모집 공고 조회 | ✅ 완료 | `RecruitController.getList` | |
| **TBD-002** | 모집 공고 작성 | ✅ 완료 | `RecruitController.create` | |
| **TBD-003** | 팀 별 모집공고 조회 | ✅ 완료 | `TeamController` | 팀 상세에서 확인 가능 |
| **TBD-004** | 공고 상세 조회 | ✅ 완료 | `RecruitController.getDetail` | |
| **TBD-005** | 공고 댓글 작성 | ✅ 완료 | `CommentController` | 커뮤니티 댓글과 통합 |
| **TBD-006** | 공고 지원 | ✅ 완료 | `RecruitController.applyRecruit` | `RecruitApply` (M:N) 구현됨 |

### 1.4 관리자 (Admin)
| 기능 ID | 기능명 | 상태 | 구현 코드 | 비고 |
|:---:|:---:|:---:|:---:|:---|
| - | 역할/권한 관리 | ✅ 완료 | `AdminController` | 명세서엔 없으나 구현됨 (RBAC) |
| **ADM-002** | 사용자 삭제 | ✅ 완료 | `AdminController.deleteUser` | Soft Delete |
| **ADM-006** | 게시글 삭제 | ✅ 완료 | `AdminController.deleteArticle` | Soft Delete |
| **ADM-007** | 공지사항 작성 | ✅ 완료 | `AdminController.createNotice` | `Article.isPinned` 활용 |

---

## 2. 미구현 및 누락 기능 (Missing Features)

명세서에는 존재하나 현재 코드(`Controller`, `Service`)가 없거나 비어있는 기능입니다.

### 2.1 인증 및 회원관리 (Critical)
*   **SGU-001 (아우누리 이메일 인증)**: `UserSignupRequest`에 이메일 필드는 있으나, 메일 발송 및 인증 코드 검증 로직이 없습니다.
*   **USR-001 (비밀번호 변경)**: 초기화 링크 전송 및 재설정 API가 없습니다.

### 2.2 기타 (Minor)
*   **TAK-004 (신고)**: 게시글/사용자 신고 기능이 전무합니다.
*   **CLG-001 (챌린지/도전과제)**: Phase 4 Part 2 예정으로 현재 미구현입니다.
*   **ADM (관리자 상세)**: 신고 처리 기능이 미구현입니다.

## 3. 결론

사용자가 직접 복구한 **회원가입 기능(SGU-002)**은 `UserController`와 `UserService`에 정상적으로 반영되어 있습니다.
다만, **이메일 인증(SGU-001)** 절차가 생략되어 있어 바로 가입이 가능한 상태입니다.
프로젝트 진행을 위해 다음 단계인 **GitHub 활동 크롤링(Challenge)** 개발로 넘어가거나, 누락된 **이메일 인증/댓글 조회** 등을 보강하는 선택이 필요합니다.
