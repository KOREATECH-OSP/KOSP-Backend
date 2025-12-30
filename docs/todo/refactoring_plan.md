# Refactoring Plan

## Admin Domain Redesign
Current admin domain is monolithic. We plan to restructure it by features.

```
admin/
├── member/           # ADM-001, ADM-002
│   ├── controller/
│   ├── service/
│   └── dto/
├── content/          # ADM-006, ADM-007, ADM-008
│   ├── controller/
│   ├── service/
│   └── dto/
├── challenge/        # ADM-003, ADM-004, ADM-005
│   ├── controller/
│   ├── service/
│   └── dto/
└── report/           # ADM-009
    ├── controller/
    ├── service/
    └── dto/
```

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

## Package Structure Refactoring
Rename base package to match domain.
- `kr.ac.koreatech.sw.kosp` -> `io.swkoreatech.kosp`
