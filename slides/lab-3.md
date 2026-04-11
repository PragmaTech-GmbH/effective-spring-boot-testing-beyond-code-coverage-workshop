---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Effective Spring Boot Testing Beyond Code Coverage

## Lab 3 — Fast & Reliable Builds

Philip Riecks — [PragmaTech GmbH](https://pragmatech.digital/) — [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Effective Spring Boot Testing Beyond Code Coverage' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 2

- WireMock stubbing — what tripped you up?
- `MOCK` vs `RANDOM_PORT` — which did you pick and why?

---

# Lab 3

## Spring Test Context Caching & Parallel Execution

---

## Build Time: The Hidden Tax

50 integration tests × 8 s context startup = **400 s wasted** if every test starts a fresh context.

Devs stop running tests locally. CI gets slower. Confidence drops.

---

## The Solution: Spring Test Context Caching

The Spring `TestContextManager` caches `ApplicationContext` instances across test classes — keyed by their **configuration**.

If two tests share the same configuration, they share the same context.

---

## What Counts as "Same Configuration"?

The cache key includes:

- `@SpringBootTest` `classes`, `properties`, `args`
- `@ActiveProfiles`, `@TestPropertySource`
- `@MockitoBean` / `@MockitoSpyBean`
- `@DynamicPropertySource` values
- `ContextCustomizer`s registered by Spring Boot

**Anything different → new context → new startup cost.**

---

## Context Cache Killers

- Sprinkling `@MockitoBean` differently across tests
- Per-test `@TestPropertySource` with unique values
- `@DirtiesContext` (almost always overkill)
- Random ports baked into properties
- Different combinations of `@Import` / `@ContextConfiguration`

---

## `@DirtiesContext` — Use With Caution

It **evicts** the context from the cache. Every subsequent test rebuilds.

If you need it, ask: *can I reset state in `@AfterEach` instead?*

---

## The Solution: Unify Context Configuration

```java
@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
public abstract class SharedIntegrationTestBase { }
```

Subclass it everywhere. One context, infinite reuse.

---

## Sliced Tests Still Matter

A `@WebMvcTest` or `@DataJpaTest` is a **smaller** context that boots faster *and* caches independently. Use slices when you don't need the full stack.

---

## New in Spring Framework 7: Pausing Contexts

Idle contexts can be **paused** to reclaim memory while keeping the cache key valid. Resume on demand.

→ Larger cache, lower memory ceiling.

---

# Test Parallelization

---

## Approach 1: Maven `forkCount` — Process Level

```xml
<configuration>
  <forkCount>1C</forkCount>
  <reuseForks>true</reuseForks>
</configuration>
```

- One JVM **per CPU core**
- Each fork has its **own** context cache
- Strong isolation, more memory

---

## Approach 2: JUnit Jupiter Parallel — Thread Level

`src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

- Multiple threads in **one** JVM
- **Shares** the context cache
- Cheaper memory, but you must write thread-safe tests

---

## Unit Tests: Writing Parallel-Safe Code

- ❌ Static mutable state
- ❌ `System.setProperty(...)`
- ❌ Shared temp files with fixed names
- ✅ `@TempDir`, fresh fixtures, local mocks

---

## Integration Tests: What to AVOID

- Hard-coded ports
- Shared rows with fixed IDs
- `@DirtiesContext` in parallel mode (race conditions)
- Mutating `@MockitoBean`s across threads

---

## Integration Tests: The Safe Pattern

- One **shared** context, **isolated** data per test
- Random IDs / per-test schemas
- Singleton Testcontainers shared across the JVM
- Cleanup in `@AfterEach`, not via `@DirtiesContext`

---

## Singleton Testcontainers Pattern

```java
public abstract class AbstractIT {
  static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer("postgres:16-alpine");
  static { POSTGRES.start(); }
}
```

One container per JVM, shared by **every** test that extends the base.
Combined with `forkCount=1C` you get *N* containers total — predictable.

---

# Time For Some Exercises

See `labs/lab-3/README.md`.

1. Find and fix the **context cache killers** in `labs/lab-3` — collect cache stats before and after
2. Enable parallel execution and measure the speedup
3. Convert the per-class containers to the singleton pattern

---

## Recap

- One context, reused = the single biggest test-suite speedup
- Mind the cache key — small differences are expensive
- Pick *one* parallelization model and make tests safe for it
- Singleton Testcontainers + shared context = fast and reliable

**Next:** mutation testing — are your tests actually catching bugs?
