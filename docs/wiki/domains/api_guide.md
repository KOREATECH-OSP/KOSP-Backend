# API 가이드 (API Guide)

KOSP 백엔드는 **RESTful API** 원칙을 준수합니다.

## 1. 기본 원칙
*   **리소스 중심**: `/v1/users`, `/v1/auth`, `/v1/community/...`
*   **응답 규격**: JSON 포맷 사용.
*   **에러 응답**: `GlobalExceptionHandler`를 통해 통일된 에러 객체 반환.

## 2. 주요 엔드포인트 요약
상세 명세는 Swagger UI 또는 `done/api_spec.md`를 참고하세요.

### Auth (`/v1/auth`)
*   `POST /login`: 로그인
*   `POST /logout`: 로그아웃
*   `GET /me`: 내 정보 조회

### User (`/v1/users`)
*   `POST /signup`: 회원가입
*   `POST /email/verify`: 인증 메일 발송
