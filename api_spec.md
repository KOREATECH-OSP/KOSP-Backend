# KOSP REST API Specification

## 개요
이 문서는 KOSP 프로젝트의 통합 REST API 명세서입니다. 모든 API는 RESTful 원칙을 따르며 JSON 형식을 사용합니다.

## 진행 현황
- [x] 1.1 로그인
- [x] 1.2 로그아웃
- [x] 1.3 내 정보 조회 (Current User)
- [ ] 2.1 사용자 정보 수정
- [ ] 2.2 사용자 상세 조회 (타인 조회)
- [ ] 2.3 사용자 활동 조회 (GitHub 활동)
- [ ] 2.4 사용자 작성 글 목록
- [ ] 3.1 게시판 목록 조회
- [ ] 3.2 게시글 목록 조회
- [ ] 3.3 게시글 상세 조회
- [ ] 3.4 게시글 작성
- [ ] 3.5 게시글 수정
- [ ] 3.6 게시글 삭제
- [ ] 3.7 댓글 목록 조회
- [ ] 3.8 댓글 작성
- [ ] 3.9 댓글 삭제
- [ ] 3.10 게시글 좋아요/북마크
- [ ] 3.11 댓글 좋아요
- [ ] 4.1 모집 공고 목록 조회
- [ ] 4.2 모집 공고 상세 조회
- [ ] 4.3 모집 공고 작성
- [ ] 4.4 모집 공고 수정
- [ ] 4.5 모집 상태 변경
- [ ] 4.6 모집 공고 삭제
- [ ] 4.7 모집 공고 댓글 목록 조회
- [ ] 4.8 모집 공고 댓글 작성
- [ ] 4.9 모집 공고 댓글 삭제
- [ ] 4.10 모집 공고 좋아요/북마크
- [ ] 4.11 모집 공고 댓글 좋아요
- [ ] 5.1 팀 목록 조회
- [ ] 5.2 팀 생성
- [ ] 5.3 팀 상세 조회
- [ ] 6.1 도전 과제 목록 및 진행도 조회

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
    "bio": "수정된 자기소개",
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
        "isRecuitmentAllowed": false
      },
      {
        "id": 2,
        "name": "정보공유",
        "description": "개발 정보를 공유합니다",
        "isRecuitmentAllowed": false
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
        "comments": 2
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
    "content": "내용"
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
    "content": "수정된 내용"
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

### 3.10 게시글 좋아요/북마크
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
  - `404 Not Found`: 게시글 없음

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
    "recruitments": [
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
        "likes": 10
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
- **Endpoint**: `GET /community/recruits/{recruitmentId}`
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
    "endDate": "2024-03-31T18:00:00"
  }
  ```
- **Response**: `201 Created`
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패

### 4.4 모집 공고 수정
- **Endpoint**: `PUT /community/recruits/{recruitmentId}`
- **Request Body**:
  ```json
  {
    "title": "수정된 제목",
    "content": "수정된 내용",
    "startDate": "2024-03-17T09:00:00",
    "endDate": "2024-04-01T18:00:00"
  }
  ```
- **Response**: `200 OK`
- **Error Response**:
  - `400 Bad Request`: 입력 형식 오류
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 수정 권한 없음
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.5 모집 상태 변경
- **Endpoint**: `PATCH /community/recruits/{recruitmentId}/status`
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
- **Endpoint**: `DELETE /community/recruits/{recruitmentId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 삭제 권한 없음
  - `404 Not Found`: 모집 공고가 존재하지 않음

### 4.7 모집 공고 댓글 목록 조회
- **Endpoint**: `GET /community/recruits/{recruitmentId}/comments`
- **Query Parameters**:
  - `lastCommentId`: number
  - `limit`: number
- **Response**: `200 OK`
  ```json
  {
    "comments": [
      {
        "id": 105,
        "author": { "id": 50, "name": "유저", "profileImage": "..." },
        "content": "문의합니다.",
        "createdAt": "...",
        "likes": 1,
        "isLiked": false,
        "isMine": false
      }
    ],
    "meta": { "hasNext": true, "lastCommentId": 105 }
  }
  ```
- **Error Response**:
  - `404 Not Found`: 모집 공고 없음

### 4.8 모집 공고 댓글 작성
- **Endpoint**: `POST /community/recruits/{recruitmentId}/comments`
- **Request Body**:
  ```json
  {
    "content": "문의 댓글입니다."
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": 202,
    "content": "문의 댓글입니다.",
    "createdAt": "...",
    "author": { "id": 1, "name": "김개발", "profileImage": "..." }
  }
  ```
- **Error Response**:
  - `400 Bad Request`: 내용 없음
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 모집 공고 없음

### 4.9 모집 공고 댓글 삭제
- **Endpoint**: `DELETE /community/recruits/{recruitmentId}/comments/{commentId}`
- **Response**: `204 No Content`
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 삭제 권한 없음
  - `404 Not Found`: 댓글 또는 모집 공고 없음

### 4.10 모집 공고 좋아요/북마크
- **Endpoint**: `POST /community/recruits/{recruitmentId}/likes`
- **Response**: `200 OK`
  ```json
  {
    "isLiked": true
  }
  ```
- **Endpoint**: `POST /community/recruits/{recruitmentId}/bookmarks`
- **Response**: `200 OK`
  ```json
  {
    "isBookmarked": true
  }
  ```
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 모집 공고 없음

### 4.11 모집 공고 댓글 좋아요
- **Endpoint**: `POST /community/recruits/{recruitmentId}/comments/{commentId}/likes`
- **Response**: `200 OK`
  ```json
  {
    "isLiked": true
  }
  ```
- **Error Response**:
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 댓글 또는 모집 공고 없음

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
      { "id": 1, "name": "김개발", "role": "LEADER" },
      { "id": 3, "name": "박팀원", "role": "MEMBER" }
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
  ```
