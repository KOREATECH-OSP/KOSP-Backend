# KOSP REST API Specification

## 개요
이 문서는 KOSP 프로젝트의 통합 REST API 명세서입니다. 모든 API는 RESTful 원칙을 따르며 JSON 형식을 사용합니다.

## 진행 현황
- [x] 1.1 로그인
- [x] 1.2 로그아웃
- [x] 1.3 내 정보 조회 (Current User)
- [x] 2.1 사용자 정보 수정
- [x] 2.2 사용자 상세 조회 (타인 조회)
- [ ] 2.3 사용자 활동 조회 (GitHub 활동)
- [x] 2.4 사용자 작성 글 목록
- [x] 3.1 게시판 목록 조회
- [x] 3.2 게시글 목록 조회
- [x] 3.3 게시글 상세 조회
- [x] 3.4 게시글 작성
- [x] 3.5 게시글 수정
- [x] 3.6 게시글 삭제
- [x] 3.7 댓글 목록 조회
- [x] 3.8 댓글 작성
- [x] 3.9 댓글 삭제
- [x] 3.10 게시글 좋아요/북마크
- [x] 3.11 댓글 좋아요
- [ ] 3.12 게시글 신고
- [x] 4.1 모집 공고 목록 조회
- [x] 4.2 모집 공고 상세 조회
- [x] 4.3 모집 공고 작성
- [x] 4.4 모집 공고 수정
- [x] 4.5 모집 상태 변경
- [x] 4.6 모집 공고 삭제
- [x] 4.7 모집 공고 댓글 목록 조회 (통합됨 -> 3.7)
- [x] 4.8 모집 공고 댓글 작성 (통합됨 -> 3.8)
- [x] 4.9 모집 공고 댓글 삭제 (통합됨 -> 3.9)
- [x] 4.11 모집 공고 댓글 좋아요 (통합됨 -> 3.11)
- [x] 5.1 팀 목록 조회
- [x] 5.2 팀 생성
- [x] 5.3 팀 상세 조회
- [ ] 6.1 도전 과제 목록 및 진행도 조회
- [x] 7.1 역할 목록 조회
- [x] 7.2 역할 생성
- [x] 7.3 역할에 정책 할당
- [x] 7.4 사용자 역할 변경
- [x] 7.10 챌린지 생성
- [x] 7.11 챌린지 삭제

---

## 1. 인증 (Auth)

### 1.1 로그인
세션 기반 인증을 사용합니다. 성공 시 세션 쿠키가 발급됩니다.

- **Endpoint**: `POST /auth/login`
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**: `200 OK`
  - *Response Headers*: `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly`
- **Error Response**:
  - `400 Bad Request`: 이메일/비밀번호 형식 오류
  - `401 Unauthorized`: 이메일 또는 비밀번호 불일치

### 1.2 로그아웃
서버 세션을 무효화하고 클라이언트 쿠키를 만료시킵니다.

- **Endpoint**: `POST /auth/logout`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 로그인되지 않은 상태

### 1.3 내 정보 조회 (Current User)
현재 로그인된 세션의 사용자 정보를 조회합니다.

- **Endpoint**: `GET /auth/me`
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "김개발",
    "profileImage": "https://...",
    "bio": "풀스택 개발자입니다."
  }
  ```

---

## 2. 사용자 (Users)

### 2.1 사용자 정보 수정

- **Endpoint**: `PUT /users/{userId}`
- **Request Body**:
  ```json
  {
    "name": "김수정",
    "introduction": "수정된 자기소개",
    "profileImage": "https://..."
  }
  ```
- **Response**: `200 OK` (Body 없음)

### 2.2 사용자 상세 조회 (타인 조회)
- **Endpoint**: `GET /users/{userId}`
- **Response**: `200 OK`
  ```json
  {
    "id": 2,
    "name": "이오픈",
    "profileImage": "...",
    "githubUrl": "...",
    "bio": "..."
  }
  ```

### 2.3 사용자 활동 조회 (GitHub 활동)
- **Endpoint**: `GET /users/{userId}/activities/github`
- **Response**: `200 OK`
  ```json
  {
    "activities": [
      {
        "id": 101,
        "type": "pr",
        "repoName": "koreatech/kosp-web",
        "title": "fix: 버그 수정",
        "date": "2024-11-28",
        "url": "https://github.com/..."
      }
    ]
  }
  ```

### 2.4 사용자 작성 글 목록
- **Endpoint**: `GET /users/{userId}/posts`
- **Response**: `200 OK`
  ```json
  {
    "posts": [
      {
        "id": 1,
        "title": "작성한 글 제목",
        "createdAt": "2024-11-28",
        "views": 10,
        "comments": 2
      }
    ]
  }
  ```

### 2.5 사용자 작성 댓글 목록
- **Endpoint**: `GET /users/{userId}/comments`
- **Response**: `200 OK`
  ```json
  {
    "comments": [
      {
        "id": 105,
        "articleId": 1,
        "articleTitle": "React 19 업데이트 정리",
        "content": "이 부분 자세히 설명해 주실 수 있나요?",
        "createdAt": "2024-03-15T13:00:00Z",
        "likes": 2,
        "isLiked": false,
        "isMine": true
      }
    ],
    "meta": {
      "hasNext": false,
      "lastCommentId": 105
    }
  }
  ```


### 2.6 사용자 즐겨찾기 목록
- **Endpoint**: `GET /users/{userId}/bookmarks`
- **Response**: `200 OK`
  ```json
  {
    "posts": [
      {
        "id": 1,
        "title": "즐겨찾기한 글 제목",
        "createdAt": "2024-11-28",
        "views": 10,
        "comments": 2
      }
    ]
  }
  ```

### 2.7 회원 탈퇴
- **Endpoint**: `DELETE /users/{userId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 본인 아님

---

## 3. 커뮤니티 (Community & Articles)
일반 게시글(자유, 정보, 홍보 등)을 관리합니다. 모집 관련 필드가 포함되지 않습니다.

### 3.1 게시판 목록 조회
- **Endpoint**: `GET /community/boards`
- **Response**: `200 OK`
  ```json
  {
    "boards": [
      {
        "id": 1,
        "name": "자유게시판",
        "description": "자유롭게 이야기하는 공간",
        "isRecruitAllowed": false
      },
      {
        "id": 2,
        "name": "정보공유",
        "description": "개발 정보를 공유합니다",
        "isRecruitAllowed": false
      }
    ]
  }
  ```

### 3.2 게시글 목록 조회
- **Endpoint**: `GET /community/articles`
- **Query Parameters**:
  - `page`: number (기본값: 1)
  - `limit`: number (기본값: 10)
  - `boardId`: number (필수)
  - `sort`: 'latest' | 'popular'
- **Response**: `200 OK`
  ```json
  {
    "posts": [
      {
        "id": 1,
        "boardId": 1,
        "title": "React 19 업데이트 정리",
        "author": { "id": 10, "name": "김개발" },
        "createdAt": "2024-03-15T12:00:00Z",
        "views": 120,
        "likes": 5,
        "comments": 2,
        "tags": ["React", "Frontend"]
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 10,
      "totalItems": 100
    }
  }
  ```

### 3.3 게시글 상세 조회
- **Endpoint**: `GET /community/articles/{articleId}`
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "boardId": 1,
    "title": "React 19 업데이트 정리",
    "content": "상세 내용...",
    "author": {
      "id": 10,
      "name": "김개발",
      "profileImage": "https://kosp.kr/images/profile/default.png"
    },
    "createdAt": "2024-03-15T12:00:00Z",
    "views": 150,
    "likes": 5,
    "comments": 2,
    "tags": ["React", "Frontend", "Update"],
    "isLiked": true,
    "isBookmarked": false
  }
  ```
- **Error Response**:
  - `404 Not Found`: 게시글이 존재하지 않음

### 3.4 게시글 작성
- **Endpoint**: `POST /community/articles`
- **Request Body**:
  ```json
  {
    "boardId": 1,
    "title": "제목",
    "content": "내용",
    "tags": ["태그1", "태그2"]
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": 101
  }
  ```
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패

### 3.5 게시글 수정
- **Endpoint**: `PUT /community/articles/{articleId}`
- **Request Body**:
  ```json
  {
    "boardId": 1,
    "title": "수정된 제목",
    "content": "수정된 내용",
    "tags": ["수정된태그1"]
  }
  ```
- **Response**: `200 OK`
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 수정 권한 없음
  - `404 Not Found`: 커뮤니티가 존재하지 않음

### 3.6 게시글 삭제
- **Endpoint**: `DELETE /community/articles/{articleId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 삭제 권한 없음
  - `404 Not Found`: 커뮤니티가 존재하지 않음

### 3.7 댓글 목록 조회
> **Note**: 모집 공고(Recruit)에 대한 댓글도 이 API를 사용합니다. `articleId`에 `recruitId`를 입력하세요.

- **Endpoint**: `GET /community/articles/{articleId}/comments`
- **Query Parameters**:
  - `lastCommentId`: number
  - `limit`: number
- **Response**: `200 OK`
  ```json
  {
    "comments": [
      {
        "id": 105,
        "author": {
            "id": 50,
            "name": "호기심유저",
            "profileImage": "https://..."
        },
        "content": "이 부분 자세히 설명해 주실 수 있나요?",
        "createdAt": "2024-03-15T13:00:00Z",
        "likes": 2,
        "isLiked": false,
        "isMine": false
      }
    ],
    "meta": {
      "hasNext": true,
      "lastCommentId": 105
    }
  }
  ```
- **Error Response**:
  - `404 Not Found`: 게시글이 존재하지 않음

### 3.8 댓글 작성
- **Endpoint**: `POST /community/articles/{articleId}/comments`
- **Request Body**:
  ```json
  {
    "content": "댓글 내용입니다."
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": 201,
    "content": "댓글 내용입니다.",
    "createdAt": "2024-03-16T10:00:00Z",
    "author": {
      "id": 1,
      "name": "김개발",
      "profileImage": "..."
    }
  }
  ```
- **Error Response**:
  - `400 Bad Request`: 내용 없음
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 게시글 없음

### 3.9 댓글 삭제
- **Endpoint**: `DELETE /community/articles/{articleId}/comments/{commentId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 삭제 권한 없음
  - `404 Not Found`: 댓글 또는 게시글 없음

### 3.10 게시글/모집 공고 좋아요/북마크
> **Note**: 모집 공고(Recruit)도 게시글(Article)의 일종이므로, 모집 공고에 대한 좋아요/북마크도 이 API를 사용합니다. `articleId`에 `recruitId`를 입력하여 요청하세요.

- **Endpoint**: `POST /community/articles/{articleId}/likes`
- **Response**: `200 OK`
  ```json
  {
    "isLiked": true
  }
  ```
- **Endpoint**: `POST /community/articles/{articleId}/bookmarks`
- **Response**: `200 OK`
  ```json
  {
    "isBookmarked": true
  }
  ```
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 게시글(또는 모집 공고) 없음

### 3.11 댓글 좋아요
- **Endpoint**: `POST /community/articles/{articleId}/comments/{commentId}/likes`
- **Response**: `200 OK`
  ```json
  {
    "isLiked": true
  }
  ```
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 댓글 또는 게시글 없음

### 3.12 게시글 신고
- **Endpoint**: `POST /community/articles/{articleId}/reports`
- **Request Body**:
  ```json
  {
    "reason": "SPAM", // "SPAM", "ABUSE", "ADVERTISEMENT", "OTHER"
    "description": "신고 사유 상세..."
  }
  ```
- **Response**: `201 Created`
- **Error Response**:
  - `400 Bad Request`: 사유 없음
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 게시글 없음

---

## 4. 모집 공고 (Recruits)
팀원 모집을 위한 전용 커뮤니티입니다. 팀 정보와 모집 기간, 상태를 포함합니다.

### 4.1 모집 공고 목록 조회
- **Endpoint**: `GET /community/recruits`
- **Query Parameters**:
  - `page`: number
  - `limit`: number
  - `status`: 'OPEN' | 'CLOSED'
  - `sort`: 'latest' | 'popular'
- **Response**: `200 OK`
  ```json
  {
    "recruits": [
      {
        "id": 2,
        "title": "사이드 프로젝트 프론트엔드 구합니다",
        "teamId": 5,
        "status": "OPEN",
        "startDate": "2024-03-16T09:00:00",
        "endDate": "2024-03-31T18:00:00", // null 가능
        "author": { "id": 12, "name": "팀리더" },
        "createdAt": "2024-03-16T12:00:00",
        "views": 300,
        "likes": 10,
        "tags": ["SideProject", "Frontend"]
      }
    ],
    "pagination": {
        "currentPage": 1,
        "totalPages": 1,
        "totalItems": 1
    }
  }
  ```

### 4.2 모집 공고 상세 조회
- **Endpoint**: `GET /community/recruits/{recruitId}`
- **Response**: `200 OK`
  ```json
  {
    "id": 2,
    "title": "사이드 프로젝트 프론트엔드 구합니다",
    "content": "상세 모집 내용...",
    "teamId": 5,
    "status": "OPEN",
    "startDate": "2024-03-16T09:00:00",
    "endDate": "2024-03-31T18:00:00",
    "author": {
        "id": 12, 
        "name": "팀리더",
        "profileImage": "https://kosp.kr/images/profile/default.png"
    },
    "createdAt": "2024-03-16T12:00:00",
    "views": 300,
    "likes": 10,
    "tags": ["SideProject", "Frontend"],
    "isLiked": false,
    "isBookmarked": true
  }
  ```
- **Error Response**:
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.3 모집 공고 작성
- **Endpoint**: `POST /community/recruits`
- **Request Body**:
  ```json
  {
    "title": "제목",
    "content": "내용",
    "teamId": 5,
    "startDate": "2024-03-16T09:00:00",
    "endDate": "2024-03-31T18:00:00",
    "tags": ["Tag1", "Tag2"]
  }
  ```
- **Response**: `201 Created`
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패

### 4.4 모집 공고 수정
- **Endpoint**: `PUT /community/recruits/{recruitId}`
- **Request Body**:
  ```json
  {
    "title": "수정된 제목",
    "content": "수정된 내용",
    "startDate": "2024-03-17T09:00:00",
    "endDate": "2024-04-01T18:00:00",
    "tags": ["UpdatedTag"]
  }
  ```
- **Response**: `200 OK`
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 수정 권한 없음
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.5 모집 상태 변경
- **Endpoint**: `PATCH /community/recruits/{recruitId}/status`
- **Request Body**:
  ```json
  {
    "status": "CLOSED"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "id": 2,
    "status": "CLOSED"
  }
  ```
- **Error Response**:
  - `400 Bad Request`: 잘못된 상태값
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.6 모집 공고 삭제
- **Endpoint**: `DELETE /community/recruits/{recruitId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 삭제 권한 없음
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.10 모집 공고 지원
- **Endpoint**: `POST /community/recruits/{recruitId}/apply`
- **Request Body**:
  ```json
  {
    "reason": "지원 동기 및 각오",
    "portfolioUrl": "https://example.com/portfolio"
  }
  ```
- **Response**: `201 Created`
- **Error Response**:
  - `400 Bad Request`: 모집이 마감되었거나 중복 지원
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 모집 공고 없음

### 4.11 모집 공고 댓글 목록 조회 (통합됨 -> 3.7)




---

## 5. 팀 (Teams)
팀 자체를 관리하는 API입니다. (모집 공고는 Communities API로 통합됨)

### 5.1 팀 목록 조회
- **Endpoint**: `GET /teams`
- **Query Parameters**:
  - `search`: string (검색어)
- **Response**: `200 OK`
  ```json
  {
    "teams": [
      {
        "id": 1,
        "name": "React 스터디",
        "description": "...",
        "memberCount": 5,
        "createdBy": "김개발",
        "imageUrl": "..."
      }
    ]
  }
  ```

### 5.2 팀 생성
- **Endpoint**: `POST /teams`
- **Request Body**:
  ```json
  {
    "name": "새로운 팀",
    "description": "팀 설명...",
    "imageUrl": "..."
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": 50
  }
  ```

### 5.3 팀 상세 조회
- **Endpoint**: `GET /teams/{teamId}`
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "name": "React 스터디",
    "description": "상세 설명...",
    "members": [
      { "id": 1, "name": "김개발", "profileImage": "...", "role": "LEADER" },
      { "id": 3, "name": "박팀원", "profileImage": "...", "role": "MEMBER" }
    ]
  }
  ```

---

## 6. 도전 과제 (Challenges)

### 6.1 도전 과제 목록 및 진행도 조회
- **Endpoint**: `GET /challenges`
- **Response**: `200 OK`
  ```json
  {
    "challenges": [
      {
        "id": 1,
        "title": "First Contribution",
        "description": "첫 기여를 완료하세요",
        "category": "contribution",
        "current": 1,
        "total": 1,
        "isCompleted": true
      }
    ],
    "summary": {
      "totalChallenges": 10,
      "completedCount": 2,
      "overallProgress": 20 // 퍼센트
    }
  }



---

## 7. 관리자 (Admin)
관리자 전용 API입니다.

### 7.1 역할 목록 조회
- **Endpoint**: `GET /admin/roles`
- **Response**: `200 OK`
  ```json
  [
    {
      "name": "ROLE_STUDENT",
      "description": "학생",
      "policies": ["StudentPolicy"]
    }
  ]
  ```

### 7.2 역할 생성
- **Endpoint**: `POST /admin/roles`
- **Request Body**:
  ```json
  {
    "name": "ROLE_MENTOR",
    "description": "멘토"
  }
  ```
- **Response**: `201 Created`

### 7.3 역할에 정책 할당
- **Endpoint**: `POST /admin/roles/{roleName}/policies`
- **Request Body**:
  ```json
  {
    "policyName": "MentorPolicy"
  }
  ```
- **Response**: `200 OK`

### 7.4 사용자 역할 변경
- **Endpoint**: `PUT /admin/users/{userId}/roles`
- **Request Body**:
  ```json
  {
    "roles": ["ROLE_STUDENT", "ROLE_MENTOR"]
  }
  ```
- **Response**: `200 OK`
  ```
  ```

### 7.5 사용자 삭제 (강제 탈퇴)
- **Endpoint**: `DELETE /admin/users/{userId}`
- **Response**: `204 No Content`

### 7.6 게시글 삭제
- **Endpoint**: `DELETE /admin/articles/{articleId}`
- **Response**: `204 No Content`

### 7.7 공지사항 작성
- **Endpoint**: `POST /admin/notices`
- **Request Body**:
  ```json
  {
    "title": "공지 제목",
    "content": "공지 내용",
    "isPinned": true
  }
  ```
- **Response**: `201 Created`

### 7.8 신고 목록 조회
- **Endpoint**: `GET /admin/reports`
- **Response**: `200 OK`
  ```json
  {
    "reports": [
      {
        "id": 1,
        "targetType": "ARTICLE",
        "targetId": 100,
        "reason": "SPAM",
        "reporterId": 10,
        "status": "PENDING"
      }
    ]
  }
  ```

### 7.9 신고 처리
- **Endpoint**: `POST /admin/reports/{reportId}`
- **Request Body**:
  ```json
  {
    "action": "DELETE_CONTENT" // or "BAN_USER", "REJECT"
  }
  ```
- **Response**: `200 OK`

### 7.10 챌린지 생성
- **Endpoint**: `POST /admin/challenges`
- **Request Body**:
  ```json
  {
    "name": "성실한 커미터",
    "description": "커밋 100회 달성",
    "condition": "#activity.totalCommits >= 100",
    "tier": 1,
    "imageUrl": "https://..."
  }
  ```
- **Response**: `201 Created`

### 7.11 챌린지 삭제
- **Endpoint**: `DELETE /admin/challenges/{challengeId}`
- **Response**: `204 No Content`

### 7.12 공지사항 삭제
- **Endpoint**: `DELETE /admin/notices/{noticeId}`
- **Response**: `204 No Content`

