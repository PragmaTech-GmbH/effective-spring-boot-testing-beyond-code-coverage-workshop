# Lab 3 - Accelerating Spring Boot Build Times

Same baseline app. The goal here is **speed without sacrificing confidence**.

## Learning Objectives

- Understand how Spring's `TestContextManager` computes the context cache key
- Identify and remove **context cache killers** that force unnecessary context rebuilds
- Extract shared test configuration into an `AbstractIntegrationTest` base class
- Enable **parallel test execution** (JUnit Jupiter thread-level and/or Maven fork-level)
- Apply the **singleton Testcontainers** pattern to share containers across contexts

## Exercises

### Exercise 1: Find the Context Cache Killers

The `exercises` package contains five integration tests (`ContextKiller1IT` through `ContextKiller5IT`). Each one tests the same trivial `GET /api/books` endpoint, but each prevents Spring from reusing a cached ApplicationContext.

1. Run `./mvnw verify` and count how many times you see "Started ContextKillerNIT" in the logs — each line means a fresh context was booted
2. Examine each class and find the single annotation or configuration that breaks caching
3. Remove or refactor each cache killer so all five classes can share one context

### Exercise 2: Extract the Shared Boilerplate

After fixing the killers, all five classes have nearly identical setup code (containers, `@DynamicPropertySource`, `@Import`, etc.).

1. Extract the common boilerplate into an `AbstractIntegrationTest` base class
2. Make each ContextKiller IT extend it — each class should be just a few lines
3. Run `./mvnw verify` again and confirm only **one** context is booted for all five classes

### Exercise 3: Enable Parallel Execution

1. Add a `junit-platform.properties` file with `parallel.mode.classes.default = concurrent`
2. Run the `experiment/Slow*Test` dummy unit tests and observe the wall-time improvement
3. **Bonus:** Try the same with the integration tests — what additional precautions are needed for parallel ITs?

## How to Run

```bash
cd labs/lab-3
./mvnw verify                    # run everything
./mvnw test -Dtest="Slow*Test"   # run only the dummy unit tests (parallelization demo)
```

## Reference Solutions

Solutions live under `src/test/java/.../lab3/solutions/`:

- `AbstractIntegrationTest` — shared base class with singleton containers, WireMock, OAuth2Stubs, and unified config
- `ContextKiller1IT` through `ContextKiller5IT` - each extends `AbstractIntegrationTest`, cache killers removed, one shared context
