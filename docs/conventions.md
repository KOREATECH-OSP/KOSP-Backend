# KOSP Project Coding Conventions & Architecture

본 문서는 프로젝트 진행 시 반드시 준수해야 할 코드 작성 규칙, 아키텍처 설계, 그리고 사용자 요청 사항을 정리한 문서입니다.
모든 개발은 이 문서를 기준으로 진행하며, 변경 사항이 있을 경우 본 문서를 업데이트해야 합니다.

---

## 1. Strict Coding Rules (엄격한 코딩 규칙)

다음 규칙들은 `task.md`에 명시된 핵심 제약 사항으로, 예외 없이 준수해야 합니다.

### 1.1 제어문 및 연산자 제한
*   **Indent Depth <= 1**: 분기문(if, for 등) 중첩을 1단계까지만 허용합니다.
    *   *Bad*: `if (condition) { if (nested) { ... } }`
    *   *Good*: `guard clauses`를 사용하여 조기 리턴(Early Return)하거나 메서드로 분리합니다.
*   **No `else` Keyword**: `else`, `else if` 사용을 금지합니다.
    *   항상 조기 리턴(Early Return) 패턴을 사용하여 흐름을 단순화합니다.
*   **No Ternary Operators**: 삼항 연산자(`? :`) 사용을 금지합니다.

### 1.2 클래스 및 메서드 구조
*   **Method Length <= 10 Lines**: 모든 메서드는 10줄 이내로 작성합니다.
    *   메서드가 길어지면 책임을 분리하여 하위 메서드나 다른 클래스로 추출해야 합니다.
*   **Max 2 Instance Variables Per Class**: 클래스 당 인스턴스 변수(필드)는 최대 2개로 제한합니다.
    *   3개 이상의 필드가 필요한 경우, 관련된 필드들을 묶어 별도의 객체(Value Object, Embedded)로 분리해야 합니다.
    *   *예외*: **Repository** 필드는 확장성에 필요한 경우 제한 없이 허용합니다.
    *   *예외*: JPA Entity의 경우 연관관계 매핑 등으로 인해 불가피할 수 있으나, 가능한 한 임베디드 타입(`@Embeddable`)을 활용하여 준수하도록 노력합니다.
*   **Small Entities**: 엔티티는 가능한 작게 유지합니다.

### 1.3 데이터 구조 및 변수
*   **Collections over Arrays**: 배열(`[]`) 대신 Java Collection Framework(`List`, `Set`, `Map` 등)를 사용합니다.
*   **No Abbreviations**: 변수명, 메서드명, 클래스명에 축약어를 사용하지 않습니다.
    *   *Bad*: `req`, `res`, `idx`
    *   *Good*: `request`, `response`, `index`
*   **Naming Convention**: 메서드 및 변수 이름은 **최대 2단어**로 구성하는 것을 권장합니다. (User Request: "Method/Variable names max 2 words")
    *   이 규칙은 가독성을 위해 간결한 명명을 지향하라는 의미로 해석됩니다. 도메인 용어가 복잡한 경우 명확성을 해치지 않는 선에서 준수합니다.

### 1.4 기타 표준
*   **High Extensibility**: 확장에 열려있는 구조를 지향합니다. (OCP)
*   **Dependency Injection**: 생성자 주입(Constructor Injection)을 사용합니다.
    *   `@RequiredArgsConstructor` Lombok 어노테이션을 적극 활용합니다.
*   **Lombok Usage**:
    *   `@Getter`, `@RequiredArgsConstructor`, `@Builder` 위주로 사용합니다.
    *   `@Setter` 사용은 지양합니다. 특히 Entity에서는 변경 의도가 명확한 비즈니스 메서드(`updateInfo` 등)를 사용합니다.

---

## 2. Architecture & Design (아키텍처 및 설계)

### 2.1 Layered Architecture
*   **Controller (`api`, `controller`)**: 요청 검증, 서비스 호출, 응답 반환. 비즈니스 로직 포함 금지.
*   **Service (`service`)**: 비즈니스 로직 수행, 트랜잭션 관리 (`@Transactional`).
*   **Repository (`repository`)**: 데이터베이스 접근. `JpaRepository` 대신 `Repository` 인터페이스를 상속받아 필요한 메서드만 노출하는 것을 권장합니다.
*   **Domain Model (`model`)**: 핵심 비즈니스 규칙과 상태를 가진 엔티티.

### 2.2 DTO (Data Transfer Object)
*   엔티티를 직접 API 응답으로 반환하지 않습니다. 반드시 `Request` / `Response` DTO를 사용합니다.
*   DTO는 `record` 타입을 적극 활용하거나, `Lombok`(`@Data`, `@Getter`)을 사용합니다.
*   **패키지 구조**: `dto/request`, `dto/response`로 구분합니다.

### 2.3 Package Structure
*   **Domain Driven**: `kr.ac.koreatech.sw.kosp.domain.{도메인명}` 하위에 기능별 패키지 구성.
    *   `api` (Interface for Swagger)
    *   `controller` (Implementation)
    *   `model` (Entity)
    *   `repository`
    *   `service`
    *   `dto` (DTO는 반드시 `record` 또는 `static inner class` 등으로 관리하여 파일 수를 줄이거나, 명확한 패키지로 분리합니다.)
*   **Global**: `kr.ac.koreatech.sw.kosp.global` 하위에 공통 기능 구성.
    *   `config`, `exception`, `security` 등.
*   **Clean Code Strategy**:
    *   코드는 읽기 쉬워야 하며, 하나의 메서드는 하나의 기능만 수행해야 합니다.
    *   주석보다는 코드 자체로 의도를 드러내도록 명명합니다.

---

## 3. Documentation & API Security

### 3.1 Swagger (OpenAPI)
*   모든 Controller는 `interface`인 **API Definition**을 별도로 가져야 합니다. (예: `UserApi` interface -> `UserController` class)
*   **Annotations**:
    *   `@Tag`: 클래스 레벨 (API 그룹)
    *   `@Operation`: 메서드 레벨 (API 상세 설명)
    *   `@Parameter`: 파라미터 설명 (Hidden 옵션 사용 가능)
*   `UserApi`와 같은 인터페이스에 Swagger 어노테이션을 작성하고, Controller는 이를 구현(`implements`)하여 코드를 깔끔하게 유지합니다.

### 3.2 Security (`@Permit`)
*   보안 인가 로직은 `@Permit` 커스텀 어노테이션을 사용하여 메서드 레벨에서 명시적으로 제어합니다.
*   **속성**:
    *   `permitAll`: `true`일 경우 인증 없이 접근 가능.
    *   `name`: 권한 이름 (RBAC용).
    *   `description`: 권한 설명.

---

## 4. Exception Handling

*   **GlobalException**: 비즈니스 로직에서 예외 발생 시 `GlobalException`을 throw 합니다.
*   **ExceptionMessage**: 에러 메시지와 상태 코드(`HttpStatus`)는 `ExceptionMessage` Enum에서 관리합니다.
    *   직접 문자열을 하드코딩하지 않고 Enum을 재사용합니다.

## 5. Development Process

*   **Implementation Plan**: 작업 전 `implementation_plan.md`를 작성하여 계획을 승인받습니다.
*   **Verification**: 구현 후 `verify` 단계를 거쳐 올바르게 동작하는지 확인합니다.
*   **Task Management**: `task.md`를 통해 진행 상황을 세밀하게 추적합니다.
