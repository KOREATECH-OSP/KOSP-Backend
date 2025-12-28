# KOSP Task List (Detailed)

본 문서는 `기능명세서 (최종) - 기능명세서.csv`와 현재 구현 상태를 1:1로 매핑하여 정리한 작업 목록입니다.
우선순위 규정:
1.  **Level 1 (Easy):** 단순 CRUD
2.  **Level 2 (Medium):** 다른 기능과 연결된 CRUD
3.  **Level 3 (Hard):** 복잡한 로직 및 설계 필요
4.  **Level 4 (Critical):** 인프라 설정 필요
5.  **Lowest:** Team Member Assignment (Email Auth)

---

## 🏗️ Priority 0: Completed Features (To Verify)

기존에 구현 완료된 것으로 확인된 기능들입니다. 최종 점검 시 에러 반환까지 확인해야 합니다.

### Auth & User
| 기능 ID | 대분류 | 소분류 | 기능명 | 상태 | 구현 코드 | 검증 사항 (에러 포함) |
|:---:|:---:|:---:|:---:|:---:|:---:|:---|
| **SGI-001** | 로그인 | 이메일 | 이메일 로그인 | ✅ 완료 | `AuthController.login` | 400(형식), 401(불일치), 미가입시 가입유도 |
| **SGI-002** | 로그인 | 깃허브 | 소셜 로그인 | ✅ 완료 | `OAuth2Controller` | 연동 계정 없음(가입유도), 서버오류 |
| **SGU-002** | 회원가입 | 계정정보 | 회원가입 처리 | ✅ 완료 | `UserController.signup` | 필드검증(학번,이름 등), 중복시 오류 |
| **USR-004** | 회원정보 | 활동 | 작성한 글 조회 | ✅ 완료 | `UserActivityController.getPosts` | 글 없을 시 빈 배열 |
| **ADM-001** | 관리자 | 사용자 | 사용자 정보 변경 | ⚠️ 부분 | `UserController.update` | 본인 정보 수정만 구현됨. 관리자 기능 추후 필요 |

### Community (Article)
| 기능 ID | 대분류 | 소분류 | 기능명 | 상태 | 구현 코드 | 검증 사항 (에러 포함) |
|:---:|:---:|:---:|:---:|:---:|:---:|:---|
| **TAK-001** | 커뮤니티 | 게시글 | 게시글 작성 | ✅ 완료 | `ArticleController.create` | 권한부족(403), 스크립트 필터링 |
| **TAK-002** | 커뮤니티 | 게시글 | 댓글 작성 | ✅ 완료 | `CommentController.create` | 게시글 없음(404) |
| **TAK-003** | 커뮤니티 | 게시글 | 즐겨찾기 | ✅ 완료 | `ArticleController.toggleBookmark` | 게시글 없음(404) |
| **TAK-005** | 커뮤니티 | 게시글 | 좋아요 | ✅ 완료 | `ArticleController.toggleLike` | 게시글 없음(404) |
| **TAK-006** | 커뮤니티 | 게시글 | 게시글 조회 | ✅ 완료 | `ArticleController.getDetail` | 게시글 없음(404), 내부 오류 |

### Recruit & Team
| 기능 ID | 대분류 | 소분류 | 기능명 | 상태 | 구현 코드 | 검증 사항 (에러 포함) |
|:---:|:---:|:---:|:---:|:---:|:---:|:---|
| **TBD-001** | 팀게시판 | 모집공고 | 공고 조회 | ✅ 완료 | `RecruitController.getList` | 권한부족(X), 내부오류 |
| **TBD-002** | 팀게시판 | 모집공고 | 공고 작성 | ✅ 완료 | `RecruitController.create` | 권한부족/조건미충족(400) |
| **TBD-003** | 팀게시판 | 모집공고 | 팀 별 공고 조회 | ✅ 완료 | `TeamController` | 팀 내 공고 리스트 확인 |
| **TBD-004** | 팀게시판 | 모집공고 | 공고 상세 조회 | ✅ 완료 | `RecruitController.getDetail` | 존재하지 않음(404) |
| **TBD-005** | 팀게시판 | 모집공고 | 공고 댓글 작성 | ✅ 완료 | `CommentController` | (커뮤니티 댓글과 통합됨) |

---

## 🚀 Priority 1: Simple CRUD (Easy)

단순 데이터 조회 및 조작 기능입니다. 가장 먼저 처리합니다.

| 기능 ID | 대분류 | 소분류 | 기능명 | 예상 난이도 | 비고 |
|:---:|:---:|:---:|:---:|:---:|:---|
| **USR-005** | 회원정보 | 활동 | 작성한 댓글 조회 | ✅ 완료 | `UserActivityController.getComments` |
| **USR-006** | 회원정보 | 활동 | 즐겨찾기 조회 | Level 1 | `UserActivityController`에 구현 예정 |
| **ADM-002** | 관리자 | 사용자 | 사용자 삭제 | Level 1 | `Soft Delete` 처리 |
| **ADM-006** | 관리자 | 게시글 | 게시글 삭제 | Level 1 | `Soft Delete` (관리자 권한) |
| **ADM-008** | 관리자 | 게시글 | 공지 삭제 | Level 1 | 단순 삭제 |

---

## 🔗 Priority 2: Connected CRUD (Medium)

다른 기능이나 엔티티와 연결되어 영향도가 있는 기능입니다.

| 기능 ID | 대분류 | 소분류 | 기능명 | 예상 난이도 | 비고 |
|:---:|:---:|:---:|:---:|:---:|:---|
| **TBD-006** | 팀게시판 | 모집공고 | 공고 지원 | Level 2 | `Recruit` <-> `User` M:N 관계 (지원 내역 테이블 필요) |
| **ADM-007** | 관리자 | 게시글 | 공지 작성 | Level 2 | 상단 고정 로직, 게시판 별 노출 범위 설정 |
| **ADM-010** | 관리자 | 점검 | 접근 제한 | Level 2 | Interceptor/Filter 수준에서 제어 필요 (Global Config) |

---

## 🧩 Priority 3: Complex Logic (Hard)

복잡한 비즈니스 로직이나 설계를 포함하는 기능입니다.

| 기능 ID | 대분류 | 소분류 | 기능명 | 예상 난이도 | 비고 |
|:---:|:---:|:---:|:---:|:---:|:---|
| **USR-002** | 회원정보 | 탈퇴 | 회원 탈퇴 | Level 3 | 유예 기간 처리를 위한 스케줄러 & 데이터 보존 정책 로직 |
| **CLG-001** | 챌린지 | 도전과제 | 깃허브 활동내역 조회 | Level 3 | GitHub API 연동 + 점수화 알고리즘 + 캐싱 |
| **ADM-003** | 관리자 | 챌린지 | 챌린지 추가 | Level 3 | 챌린지 조건(Condition) 동적 파싱 및 검증 로직 |
| **ADM-004** | 관리자 | 챌린지 | 챌린지 삭제 | Level 3 | 진행 중인 유저 데이터 처리 (정합성 보장) |
| **ADM-005** | 관리자 | 챌린지 | 챌린지 수정 | Level 3 | 조건 수정 시 기존 달성자 처리 문제 |

---

## 🛠️ Priority 4: Infrastructure / AI (Critical)

외부 서비스 연동이나 인프라 설정이 필요한 기능입니다.

| 기능 ID | 대분류 | 소분류 | 기능명 | 예상 난이도 | 비고 |
|:---:|:---:|:---:|:---:|:---:|:---|
| **TAK-004** | 커뮤니티 | 게시글 | 신고 (AI 분석) | Level 4 | LLM API 연동 및 비동기 분석 처리 |
| **ADM-009** | 관리자 | 신고 | 신고 처리 | Level 4 | 신고 데이터 집계 및 자동/수동 제재 로직 |

---

## 💤 Priority Lowest: Team Assignment

팀원이 담당하기로 한 기능입니다. 구현 우선순위에서 배제합니다.

| 기능 ID | 대분류 | 소분류 | 기능명 | 상태 | 비고 |
|:---:|:---:|:---:|:---:|:---:|:---|
| **SGU-001** | 회원가입 | 재학생 | 아우누리 이메일 인증 | ⏳ 대기 | 팀원 구현 예정 (Mail Sender) |
| **USR-001** | 회원정보 | 비밀번호 | 비밀번호 변경 | ⏳ 대기 | 이메일 발송 필요하므로 후순위 |

---

## Next Action Plan (Suggested)

**Step 1: Priority 1 (Simple CRUD) 구현**
1.  `UserActivityController` (USR-005): 내 댓글 목록
2.  `UserActivityController` (USR-006): 즐겨찾기 목록 (예정)
3.  `AdminController` 추가: 사용자 삭제, 공지 삭제

**Step 2: Priority 2 (Connected) 구현**
1.  `RecruitApplyService` (TBD-006): 모집 공고 지원하기
2.  `NoticeService` (ADM-007): 공지사항 로직

**Step 3: Priority 3 (Complex / GitHub)**
1.  GitHub API 연동 및 챌린지 시스템 구축
