# Refactoring Plan

## Admin Domain Redesign
**Status**: ✅ **COMPLETED** (2026-01-04)  
**Structure**: Feature-based sub-packages (member, content, challenge, report, role, search)  
**Swagger Organization**: ✅ Completed - 6 feature groups

### Completed Structure
```
admin/
├── member/           # User management (3 endpoints)
│   ├── api/AdminMemberApi.java
│   ├── controller/AdminMemberController.java
│   └── dto/request/
├── content/          # Article & Notice management (3 endpoints)
│   ├── api/AdminContentApi.java
│   ├── controller/AdminContentController.java
│   └── dto/request/
├── challenge/        # Challenge CRUD (3 endpoints)
│   ├── api/AdminChallengeApi.java
│   └── controller/AdminChallengeController.java
├── report/           # Report handling (2 endpoints)
│   ├── api/AdminReportApi.java
│   ├── controller/AdminReportController.java
│   └── dto/
├── role/             # Role & Policy management (5 endpoints)
│   ├── api/AdminRoleApi.java
│   ├── controller/AdminRoleController.java
│   └── dto/
└── search/           # Unified search (1 endpoint)
    ├── api/AdminSearchApi.java
    ├── controller/AdminSearchController.java
    └── dto/response/
```

**Results**:
- 29 Java files across 6 feature packages
- All tests passing
- Compilation successful

## Global Code Convention Enforcement
Ensure strict adherence to project conventions across the entire codebase.

### 1. Import Rules
- **No Wildcards**: Replace all wildcard imports (`*`) with explicit class imports.
- **Explicit Imports**: Use explicit imports instead of fully qualified names in code (unless necessary for conflict resolution).
- **Organization**: Follow the grouping order: standard java -> frameworks (spring) -> project classes.

### 2. Repository Conventions
- **Interface Inheritance**: Extend `org.springframework.data.repository.Repository` instead of `JpaRepository`.
- **Explicit Method Declaration**: explicit methods for `save`, `findById`, `findAll`, etc., to expose only allowed operations.
- **Null Safety**: When a query must return a value (not null), use a `default getBy...` method that throws an exception if the entity is not found (e.g., `default User getByEmail(String email) { ... orElseThrow(...) }`).

### 3. API Documentation & Validation
- **Swagger**: Apply Swagger annotations (`@Operation`, `@Parameter`, etc.) to all Controller endpoints and DTOs.
- **Validation**: Use `@Valid` and validation annotations (`@NotNull`, `@Size`, etc.) on DTOs and Controller parameters.
- **Security Permissions**:
    - [ ] Assign explicit `name` attributes to all `@Permit` annotations (e.g., `@Permit(name = "auth:login", ...)`) to standardize permission management. Currently many are missing names.

## Package Structure Refactoring
Rename base package to match domain.
- `kr.ac.koreatech.sw.kosp` -> `io.swkoreatech.kosp`

## Documentation Updates
- [ ] **Architecture Diagrams**: Replace temporary AI-generated schematic images (`auth_security_flow.png`, `general_app_flow.png`) with clean, professional diagrams (e.g., using Figma or Lucidchart).

## Feature Enhancements
- [ ] **Global Search Integration**: Implement a unified search endpoint for general users (`/v1/search`) to search across articles, recruits, teams, and challenges (excluding Admin-only data).
- [ ] **Admin Role/Policy Management**: Implement Update and Delete APIs for Roles and Policies in the Admin domain to support full RBAC management.
- [ ] **Email Verification Logic**: Update `sendCertificationMail` to verify duplicates. 
    - If email exists AND `is_deleted = false`: Do NOT send mail (throw UserAlreadyExists).
    - If email exists AND `is_deleted = true`: Allow sending mail (for reactivation context).
    - If email does not exist: Allow sending mail.
