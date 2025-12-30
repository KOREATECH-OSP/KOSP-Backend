# 모집 공고 목록 조회 (Recruit List)

## 📡 API Specification
**`GET /v1/community/recruits`**

*   **Description**: 현재 모집 중인 공고들을 조회합니다. 페이징 및 상태 필터링을 지원합니다.
*   **Permission Name**: `recruit:list`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Request (Query Parameters)
*   `page`: `Integer` (Default: 0)
*   `size`: `Integer` (Default: 10)
*   `status`: `OPEN` (모집중) / `CLOSED` (마감) / `ALL` (전체)
*   `teamId`: `Long` (Optional, 특정 팀의 공고만 조회)

### Response
*   **200 OK**
```json
{
  "recruits": [
    {
      "id": 5,
      "team": { "name": "KOSP팀" },
      "title": "백엔드 개발자 구인",
      "status": "OPEN",
      "endDate": "2025-01-31T23:59:59"
    }
  ],
  "pagination": { ... }
}
```

*   **404 Not Found**
```json
{
  "code": "BOARD_NOT_FOUND",
  "message": "게시판을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.getList`
*   **Service**: `RecruitService.getList`
*   **Flow**:
1. `BoardService`를 통해 `boardId`로 게시판 조회 (없을 시 404).
2. `RecruitRepository.findByBoard`로 공고 목록 페이징 조회.
3. 각 공고의 `isLiked`/`isBookmarked` 상태 확인.
4. `RecruitListResponse` (목록 + Pagination) 반환.
