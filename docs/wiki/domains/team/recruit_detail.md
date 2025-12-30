# 모집 공고 상세 조회 (Recruit Detail)

## 📡 API Specification
**`GET /v1/community/recruits/{id}`**

*   **Description**: 모집 공고의 상세 내용을 조회합니다.
*   **Permission Name**: `recruit:read`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Response
*   **200 OK**
```json
{
  "id": 5,
  "team": { "name": "KOSP팀", "leader": "홍길동" },
  "title": "백엔드 개발자 구인",
  "content": "상세 내용...",
  "status": "OPEN",
  "id": 1,
  "title": "Backend Dev Wanted",
  ...
}
```

*   **404 Not Found**
```json
{
  "code": "RECRUIT_NOT_FOUND",
  "message": "모집 공고를 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.getOne`
*   **Service**: `RecruitService.getOne`
*   **Flow**:
1. `RecruitRepository`에서 ID로 공고 조회 (없을 시 Exception).
2. 조회수 증가.
3. `isLiked`, `isBookmarked` 여부 로드.
4. `RecruitResponse` DTO 변환 및 반환.
