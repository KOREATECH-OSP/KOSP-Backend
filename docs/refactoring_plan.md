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
