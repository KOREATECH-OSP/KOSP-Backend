# 🏃 실행 가이드 (Getting Started)

KOSP 백엔드 프로젝트를 로컬 환경에서 설정하고 실행하는 방법입니다.

## 1. 사전 요구사항 (Prerequisites)
이 프로젝트는 다음 환경에서 개발되었습니다.

*   **Java**: 17 (LTS)
*   **Docker & Docker Compose**: DB 컨테이너 실행용
    *   MySQL 8.4
    *   MongoDB 8.2
    *   Redis 8.1
*   **AWS 계정 (Optional)**: 메일 발송 기능 사용 시 (SES)

## 2. 프로젝트 클론 및 설정

### 2.1 저장소 복제
```bash
git clone https://github.com/koreatech-osp/kosp-backend.git
cd kosp-backend
```

### 2.2 환경 변수 설정
`src/main/resources/application.yml`은 보안을 위해 민감한 정보를 환경변수로 관리합니다.
IDE 실행 설정 또는 OS 환경변수에 다음 값들을 설정해주세요.

| 환경변수 | 설명 | 예시 |
| :--- | :--- | :--- |
| `AWS_ACCESS_KEY` | AWS SES 접근 키 | `AKIA...` |
| `AWS_SECRET_KEY` | AWS SES 시크릿 키 | `secret...` |

> 💡 **Tip**: 로컬 개발 시에는 `application-local.yml`을 만들어 오버라이딩하는 것을 권장합니다.

## 3. 로컬 인프라 실행 (Docker)
프로젝트 루트에 포함된 `docker-compose.yml`을 사용하여 의존성 컨테이너를 실행합니다.

```bash
cd infra/db
sudo docker compose up -d
```
이 명령어는 MySQL, MongoDB, Redis 컨테이너를 백그라운드에서 실행합니다.

## 4. 애플리케이션 빌드 및 실행

### 4.1 Gradle 빌드
```bash
./gradlew clean build -x test
```
(테스트를 포함하려면 `-x test` 제거)

### 4.2 실행
```bash
./gradlew bootRun
```

### 4.3 확인
서버가 정상적으로 실행되면 다음 주소에서 API 문서를 확인할 수 있습니다.
*   **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
