# Lab 2 - Writing Reliable Integration Tests Part II

Builds on Lab 1. Same baseline app, but the focus shifts to **stubbing external HTTP calls**, **OAuth2 security in tests**, and the difference between **MOCK vs RANDOM_PORT**.

## Learning Objectives

- Stub the OpenLibrary HTTP dependency with **WireMock**
- Provide a valid **signed JWT** without running Keycloak (using `OAuth2Stubs`)
- Understand the difference between `MOCK` and `RANDOM_PORT` web environments
- Understand why `@Transactional` rollback only works with `MOCK` mode (same-thread dispatch)
- Use **Spring Security Test** (`jwt().authorities(...)`) for MockMvc-based tests

## Exercises

### Exercise 1: `ExerciseCreateBookWireMockIT`

Write a `RANDOM_PORT` integration test for `POST /api/books` that:

1. Starts a PostgreSQL Testcontainers and a WireMockServer
2. Stubs the OpenLibrary API response with WireMock (use `OpenLibraryStubs` helper)
3. Stubs OIDC discovery + JWKS on the same WireMock instance (use `OAuth2Stubs` helper)
4. Sends a POST request with a signed JWT carrying `SCOPE_books:write`
5. Asserts `201 Created` with a `Location` header
6. Verifies the persisted book has enriched metadata (title, author, thumbnailUrl)
7. Cleans up the database in `@AfterEach` (no `@Transactional` rollback with `RANDOM_PORT`)

**Hints:**
- Look at `experiment/OAuth2Stubs` for minting JWTs and `experiment/OpenLibraryStubs` for WireMock helpers
- Override `FallbackOpenLibraryApiClient` with a `@TestConfiguration` + `@Primary` real `OpenLibraryApiClient`
- Wire `book.metadata.api.url` and `spring.security.oauth2.resourceserver.jwt.issuer-uri` via `@DynamicPropertySource`

## Explore the Experiment Package

The `experiment` package contains fully working demos you can study and run:

- `BookControllerMockMvcIT` — MockMvc + `@Transactional` rollback + Spring Security Test (`jwt()`)
- `BookControllerOAuth2StubIT` — RANDOM_PORT + `OAuth2Stubs` replacing Keycloak
- `WireMockAdvancedTest` — response templating, stateful scenarios, request verification, slow responses
- `OpenLibraryStubs` / `OAuth2Stubs` — POJO helpers that wrap WireMock stubbing DSL

## How to Run

```bash
cd labs/lab-2
./mvnw verify
```

## Reference Solutions

Solutions live under `src/test/java/.../lab2/solutions/`:

- `SolutionCreateBookWireMockIT` — full `POST /api/books` IT with RANDOM_PORT, WireMock, OAuth2Stubs, and explicit cleanup
