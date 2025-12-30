# 📚 도메인 기능 목록 (Domain Feature Matrix)

각 도메인별 API 상세 명세 문서를 확인하실 수 있습니다. 기능명을 클릭하면 상세 내용으로 이동합니다.

---

## 1. 🔐 인증 (Auth)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **SGI-001** | [**이메일 로그인**](auth/login.md) | `POST` | `/v1/auth/login` |
| **SGI-002** | [**소셜 로그인 (Redirect)**](auth/social_login.md) | `GET` | `/login/oauth2/code/{provider}` |
| **SGI-003** | [**로그아웃**](auth/logout.md) | `POST` | `/v1/auth/logout` |
| **SGI-004** | [**이메일 인증코드 발송**](auth/email_send.md) | `POST` | `/v1/auth/email/verify` |
| **SGI-004** | [**이메일 인증 확인**](auth/email_confirm.md) | `POST` | `/v1/auth/email/verify/confirm` |
| **SGI-005** | [**비밀번호 재설정 발송**](auth/password_reset_request.md) | `POST` | `/v1/auth/password/reset` |
| **SGI-005** | [**비밀번호 재설정 확인**](auth/password_reset_confirm.md) | `POST` | `/v1/auth/password/reset/confirm` |

## 2. 👤 사용자 (User)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **SGU-001** | [**회원가입**](user/signup.md) | `POST` | `/v1/users/signup` |
| **USR-003** | [**프로필 조회**](user/profile_read.md) | `GET` | `/v1/users/{userId}` |
| **USR-007** | [**프로필 수정**](user/profile_update.md) | `PUT` | `/v1/users/{userId}` |
| **USR-002** | [**회원 탈퇴**](user/withdraw.md) | `DELETE` | `/v1/users/{userId}` |
| **USR-001** | [**비밀번호 변경 (로그인)**](user/password_change.md) | `PUT` | `/v1/users/me/password` |

## 3. 💬 커뮤니티 (Community)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **TAK-001** | [**게시글 작성**](community/article_create.md) | `POST` | `/v1/community/articles` |
| **TAK-002** | [**게시글 목록 조회**](community/article_list.md) | `GET` | `/v1/community/articles` |
| **TAK-002** | [**게시글 상세 조회**](community/article_detail.md) | `GET` | `/v1/community/articles/{id}` |
| **TAK-00X** | [**게시글 수정**](community/article_update.md) | `PUT` | `/v1/community/articles/{id}` |
| **TAK-00X** | [**게시글 삭제**](community/article_delete.md) | `DELETE` | `/v1/community/articles/{id}` |
| **TAK-003** | [**댓글 작성**](community/comment_create.md) | `POST` | `/v1/community/articles/{articleId}/comments` |
| **TAK-003** | [**댓글 목록 조회**](community/comment_list.md) | `GET` | `/v1/community/articles/{articleId}/comments` |
| **TAK-00X** | [**댓글 삭제**](community/comment_delete.md) | `DELETE` | `/v1/community/articles/{articleId}/comments/{commentId}` |
| **TAK-004** | [**게시글 좋아요**](community/interaction_like.md) | `POST` | `/v1/community/articles/{id}/likes` |
| **TAK-004** | [**댓글 좋아요**](community/interaction_comment_like.md) | `POST` | `/v1/community/articles/{id}/comments/{commentId}/likes` |
| **TAK-004** | [**게시글 북마크**](community/interaction_bookmark.md) | `POST` | `/v1/community/articles/{id}/bookmarks` |
| **TAK-005** | [**게시글 신고**](community/report_create.md) | `POST` | `/v1/community/articles/{articleId}/reports` |

## 4. 🤝 팀 & 채용 (Team)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **TBD-001** | [**팀 생성**](team/team_create.md) | `POST` | `/v1/teams` |
| **TBD-001** | [**팀 목록 조회**](team/team_list.md) | `GET` | `/v1/teams` |
| **TBD-001** | [**팀 상세 조회**](team/team_detail.md) | `GET` | `/v1/teams/{teamId}` |
| **TBD-002** | [**모집 공고 작성**](team/recruit_create.md) | `POST` | `/v1/community/recruits` |
| **TBD-002** | [**모집 공고 목록 조회**](team/recruit_list.md) | `GET` | `/v1/community/recruits` |
| **TBD-002** | [**모집 공고 상세 조회**](team/recruit_detail.md) | `GET` | `/v1/community/recruits/{id}` |
| **TBD-00X** | [**모집 공고 수정**](team/recruit_update.md) | `PUT` | `/v1/community/recruits/{id}` |
| **TBD-00X** | [**모집 공고 삭제**](team/recruit_delete.md) | `DELETE` | `/v1/community/recruits/{id}` |
| **TBD-00X** | [**모집 상태 변경**](team/recruit_status.md) | `PATCH` | `/v1/community/recruits/{id}/status` |
| **TBD-003** | [**모집 지원하기**](team/recruit_apply.md) | `POST` | `/v1/community/recruits/{recruitId}/apply` |

## 5. 🏆 챌린지 (Challenge)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **CLG-001** | [**챌린지 목록 조회**](challenge/challenge_list.md) | `GET` | `/v1/challenges` |
| **CLG-002** | [**활동 평가(갱신)**](challenge/challenge_evaluate.md) | `POST` | `(Planned)` |

## 6. 🐙 깃허브 (GitHub)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **GIT-001** | [**GitHub 활동 분석**](github/analysis_read.md) | `GET` | `/v1/github/users/{username}/analysis` |
| **GIT-002** | [**커밋 트렌드 조회**](github/trend_read.md) | `-` | `(Planned)` |

## 7. 🛠️ 관리자 (Admin)
| ID | Feature | Method | Endpoint |
| :---: | :--- | :---: | :--- |
| **ADM-001** | [**사용자 통합 검색**](admin/admin_search.md) | `GET` | `/v1/admin/search` |
| **ADM-001** | [**사용자 정보 수정 (관리자)**](admin/admin_user_update.md) | `PUT` | `/v1/admin/users/{userId}` |
| **ADM-002** | [**사용자 정지/삭제**](admin/admin_user_delete.md) | `DELETE` | `/v1/admin/users/{userId}` |
| **ADM-003** | [**공지사항 작성**](admin/admin_notice_create.md) | `POST` | `/v1/admin/notices` |
| **ADM-003** | [**공지사항 삭제**](admin/admin_notice_delete.md) | `DELETE` | `/v1/admin/notices/{noticeId}` |
| **ADM-009** | [**신고 접수 목록**](admin/admin_report_list.md) | `GET` | `/v1/admin/reports` |
| **ADM-009** | [**신고 처리**](admin/admin_report_process.md) | `POST` | `/v1/admin/reports/{reportId}` |
| **ADM-0XX** | [**챌린지 생성**](admin/admin_challenge_create.md) | `POST` | `/v1/admin/challenges` |
