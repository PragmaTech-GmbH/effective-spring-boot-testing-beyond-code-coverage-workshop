# Lab 3 — Fast & Reliable Builds: Context Caching + Parallel Execution

Same baseline app. The goal here is **speed without sacrificing realism**.

## Learning Objectives

- Reason about Spring's `TestContextManager` cache key
- Identify and remove **context cache killers**
- Choose between Maven `forkCount` and JUnit Jupiter parallel execution
- Apply the singleton-Testcontainers pattern

## Exercises

1. The starter test suite intentionally creates **multiple** contexts. Find every cache killer, unify the configuration into a `SharedIntegrationTestBase`, and report the before/after context count.
2. Enable parallel execution (pick `forkCount=1C` *or* JUnit Jupiter parallel) and measure the wall-clock speedup.
3. Convert `PostgresTestcontainer` and the Mailpit container to a singleton pattern shared across the JVM.

## Hints

- Run with `-Dspring.test.context.cache.maxSize=1` to surface cache misses faster.
- Add a JUnit `LauncherSessionListener` to log how many contexts the run created.

## How to Run

```bash
./mvnw verify
./mvnw -T 1C verify     # parallel forks
```

## Reference Solutions

Worked solutions live under `src/test/java/.../lab3/solutions/`:

- `SharedIntegrationTestBase` — single base class with a singleton Postgres container; every other solution extends it
- `Solution1ContextCacheKillersIT` — same configuration, no cache killers, one cached context
- `Solution2ParallelExecutionIT` — parallel-safe test (unique ISBNs, no static mutation, no `@DirtiesContext`)
- `Solution3SingletonContainersIT` — verifies one Postgres container is shared across the whole JVM
