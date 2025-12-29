# KOSP System Architecture

## 1. Overview
This document outlines the architectural decisions, domain structure, and security mechanisms of the KOSP project.

---

## 2. Domain Structure (Community & Board)

### 2.1 Concept
To support extensibility without code changes, KOSP uses a **Board-based** structure instead of hardcoded Enums for categories.

### 2.2 Entity Relationships (ERD)

```mermaid
erDiagram
    BOARD ||--o{ ARTICLE : "1:N"
    ARTICLE ||--o| RECRUITMENT : "1:1 Extension (Shared PK)"
    ARTICLE ||--o{ COMMENT : "1:N"

    BOARD {
        Long id PK
        String name "Free, Info, Promotion..."
        String description
        Boolean is_recruitment_allowed "Allow recruitment?"
    }

    ARTICLE {
        Long id PK "Shared ID"
        Long board_id FK "References BOARD.id"
        String title
        String content
        Long author_id
        int views
        int likes
        boolean is_deleted "Soft Delete"
        boolean is_pinned "Notice"
    }

    RECRUITMENT {
        Long article_id PK, FK "References ARTICLE.id"
        Long team_id
        LocalDateTime start_date
        LocalDateTime end_date
        String status
    }
```

### 2.3 Key Components
*   **Board**: Metadata for categorization. Admins can create new boards dynamically.
*   **Article**: Base entity for all posts. Uses `board_id`.
*   **Recruitment**: Extension of Article for team recruiting. Available only if `Board.is_recruitment_allowed` is true.

---

## 3. Authorization System (RBAC + @Permit)

### 3.1 Overview
Combines **Redis Pub/Sub** for real-time updates and **AOP** for zero-overhead runtime checks.

### 3.2 Architecture
```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Aspect (@Permit)
    participant SecurityContext
    
    Client->>Controller: API Request
    Controller->>Aspect: Intercept (@Permit)
    
    alt permitAll = true
        Aspect->>Controller: Pass
    else permitAll = false
        Aspect->>SecurityContext: Check Authentication
        
        opt Not Authenticated
            Aspect-->>Client: 401 Unauthorized
        end
        
        alt name is empty
            Aspect->>Controller: Pass (Auth only)
        else specific permission required
            Aspect->>PermissionService: hasPermission(user, "name")
            
            alt Has Permission
                Aspect->>Controller: Pass
            else No Permission
                Aspect-->>Client: 403 Forbidden
            end
        end
    end
```

### 3.3 Security Components
1.  **`@Permit`**: Annotate controllers to define security rules.
    *   `permitAll = true`: Public access.
    *   `name = "domain:action"`: Requires specific permission (RBAC).
2.  **`PermissionAspect`**: Runtime AOP guard using `SecurityContextHolder`.
3.  **`PermissionService`**: Transactional service to traverse `User -> Role -> Policy -> Permission`.
4.  **`PermissionInitializer`**: Auto-registers permissions defined in code to DB on startup (preserves existing policies).
