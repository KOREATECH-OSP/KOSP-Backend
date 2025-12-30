# 시작 가이드 (Getting Started)

프로젝트를 로컬 환경에서 실행하기 위한 가이드입니다.

## 1. 사전 요구사항 (Prerequisites)
*   **JDK 17** 이상
*   **Docker & Docker Compose** (Redis, MongoDB 실행용)
*   **Git**

## 2. 환경 변수 설정 (Environment Variables)
`src/main/resources/application.yml` (또는 환경변수)에 다음 설정이 필요합니다.
*   `aws.ses.access-key`: AWS IAM Access Key (ses:SendEmail 권한 필요)
*   `aws.ses.secret-key`: AWS IAM Secret Key
*   `spring.data.redis.host`: localhost (기본값)
*   `spring.datasource.url`: MySQL 접속 정보

## 3. 실행 방법 (How to Run)

### 3.1 인프라 실행 (Docker)
로컬 데이터베이스(Redis, MongoDB)를 실행합니다.
```bash
docker-compose up -d
```

### 3.2 애플리케이션 빌드 및 실행
```bash
# 빌드 (테스트 포함)
./gradlew build

# 실행
./gradlew bootRun
```
서버가 정상적으로 실행되면 `http://localhost:8080/swagger-ui.html`에서 API 문서를 확인할 수 있습니다.
