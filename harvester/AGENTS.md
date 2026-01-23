# AGENTS.md - Harvester Module

**GitHub Data Mining Microservice** — Spring Batch-based worker for collecting GitHub activity and calculating contribution scores.

---

## Overview

Standalone Spring Boot application that:
1. Listens to Redis Stream triggers from backend
2. Mines GitHub data (commits, PRs, issues) via GraphQL
3. Calculates contribution scores
4. Publishes results back to backend via Redis Stream

---

## Structure

```
io.swkoreatech.kosp.harvester
├── job/                    # Spring Batch job config
├── collection/
│   ├── step/impl/          # Mining steps (Commit, PR, Issue, etc.)
│   ├── document/           # MongoDB documents
│   └── repository/         # MongoDB repositories
├── client/                 # GitHub API clients (GraphQL, REST)
├── trigger/                # Redis Stream listeners
├── launcher/               # Priority-based job launcher
├── statistics/             # Platform-wide statistics
└── config/                 # Redis, Async, Scheduler configs
```

---

## Commands

```bash
./gradlew :harvester:bootRun         # Start harvester
./gradlew :harvester:test            # Run tests
```

---

## Key Components

### Job Pipeline (7 steps)
```
RepositoryDiscoveryStep → PullRequestMiningStep → IssueMiningStep 
→ CommitMiningStep → StatisticsAggregationStep → ScoreCalculationStep → CleanupStep
```

### StepProvider Pattern
```java
public interface StepProvider {
    String getName();
    Step getStep();
}

@Component
public class CommitMiningStep implements StepProvider {
    @Override
    public String getName() { return "commitMiningStep"; }
    
    @Override
    public Step getStep() {
        return stepBuilderFactory.get(getName())
            .tasklet(this::execute)
            .build();
    }
}
```

### Priority Job Launcher
```java
// HIGH priority: User-triggered refresh
// NORMAL priority: Scheduled batch
launcher.launch(job, params, Priority.HIGH);
```

---

## Redis Streams

| Stream | Direction | Purpose |
|--------|-----------|---------|
| `github:collection:trigger` | backend → harvester | Trigger data collection |
| `kosp:challenge-check` | harvester → backend | Notify score updates |

---

## Unique Conventions

| Pattern | Description |
|---------|-------------|
| **GraphQL-first** | Prefer `GithubGraphQLClient` over REST for bulk queries |
| **Rate Limit Aware** | `RateLimitManager` handles GitHub API quotas |
| **MongoDB for Activity** | Commits, PRs, Issues stored in MongoDB documents |
| **Single-thread Executor** | Jobs run sequentially via `PriorityBlockingQueue` |

---

## Anti-Patterns (THIS MODULE)

| ❌ Don't | ✅ Do |
|----------|-------|
| Call GitHub REST for bulk data | Use GraphQL with pagination |
| Ignore rate limits | Check `RateLimitManager` before API calls |
| Run jobs in parallel | Use `PriorityJobLauncher` queue |
| Store activity in MySQL | Use MongoDB `*Document` classes |

---

## Where to Look

| Task | Location |
|------|----------|
| Add new data type (Discussions, Stars) | Create new `*MiningStep` in `step/impl/` |
| Modify score calculation | `ScoreCalculationStep` |
| Adjust Redis communication | `trigger/CollectionTriggerListener` |
| Change GitHub queries | `resources/graphql/*.graphql` |
