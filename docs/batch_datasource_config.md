# Spring Batch Datasource Configuration Guide

## application.yml 설정 추가

다음 설정을 `application.yml`에 추가하세요:

```yaml
spring:
  # Batch 설정
  batch:
    datasource:
      url: jdbc:mysql://localhost:3306/kosp_batch?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc:
      initialize-schema: never  # V2 migration으로 이미 생성됨
    job:
      enabled: false  # Scheduler로만 실행 (자동 실행 방지)
```

## 환경 변수

다음 환경 변수가 필요합니다:

- `DB_USERNAME`: MySQL 사용자명
- `DB_PASSWORD`: MySQL 비밀번호

## 데이터베이스 생성

`kosp_batch` 데이터베이스는 V2 migration에서 이미 생성되었습니다.

```sql
CREATE DATABASE kosp_batch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 확인 사항

1. ✅ V2 migration 실행 완료 (BATCH 테이블 생성)
2. ✅ kosp_batch DB 존재
3. ⚠️ application.yml에 위 설정 추가 필요
4. ⚠️ 환경 변수 설정 필요
