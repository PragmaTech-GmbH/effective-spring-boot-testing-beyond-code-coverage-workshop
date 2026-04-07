# Lab 2 — Two Modes of `@SpringBootTest`: WireMock + Security

Builds on Lab 1. Same baseline app (now sending an email when a book is deleted), but the focus shifts to the **HTTP boundary** and **OAuth2**.

## Learning Objectives

- Stub the OpenLibrary HTTP dependency with **WireMock**
- Provide a valid **JWT** for an integration test (three strategies)
- Pick `MOCK` vs `RANDOM_PORT` deliberately
- Understand the limits of `@Transactional` for integration tests

## Exercises

1. Write a full integration test for `POST /api/books` that stubs OpenLibrary with WireMock and asserts the persisted `thumbnailUrl`.
2. Add an `Authorization: Bearer …` header with a self-signed test JWT and make `SecurityConfig` accept it.
3. Convert one of your tests from `MOCK` to `RANDOM_PORT` — observe what breaks (especially `@Transactional` cleanup) and fix it.

## How to Run

```bash
./mvnw verify
```

## Reference Solutions

Worked solutions live under `src/test/java/.../lab2/solutions/`:

- `Solution1WireMockCreateBookIT` — full `POST /api/books` IT with WireMock-stubbed OpenLibrary
- `Solution2RealJwtIT` — step-by-step guide for migrating to OAuth2 Resource Server and signing your own JWT (delivered as a documentation-only stub because the starter app uses HTTP Basic; converting `SecurityConfig` is part of the live walkthrough)
- `Solution3MockVsRandomPortIT` — `RANDOM_PORT` test with `@AfterEach` cleanup instead of `@Transactional` rollback
