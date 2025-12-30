# 메일 도메인 (Mail Domain)

## 1. 개요
회원가입 인증, 비밀번호 재설정 링크 발송 등 시스템 알림을 담당합니다.
**AWS SES**를 사용하며, **Thymeleaf 동적 템플릿**을 적용했습니다.

## 2. 아키텍처 (Event-Driven)
메일 발송은 시간이 걸리는 작업이므로, 메인 트랜잭션과 분리하여 **비동기 이벤트**로 처리합니다.

1.  **Event Publisher**: 비즈니스 로직(예: `UserService`)에서 `EmailVerificationSendEvent` 발행.
2.  **Event Listener**: `EmailEventListener`가 이벤트를 구독(Subscribe)하고, 메일 폼을 생성하여 전송 요청.
3.  **Mail Sender**: `EmailService`가 `SesMailSender`를 통해 AWS로 요청 전송.

## 3. 주요 기능 로직

### 3.1 이메일 인증 (Verification)
1.  인증 코드(6자리 숫자) 생성 후 Redis 저장 (TTL 5분).
2.  사용자에게 메일 발송.
3.  사용자가 코드 입력 -> `verifyCode` API 호출.
4.  일치 시 **TTL을 30분으로 연장** (회원가입 폼 작성 시간 확보) 및 `isVerified=true` 마킹.
5.  회원가입 완료 시 데이터 삭제.

### 3.2 템플릿 처리 (Thymeleaf)
*   **위치**: `src/main/resources/mail/`
*   **Layout**: `layout.html`을 공통 레이아웃으로 사용 (`th:replace`).
*   **동적 링크**: `@ServerURL`을 통해 주입받은 호스트 정보를 사용하여 링크 생성 (로컬/운영 환경 자동 대응).
