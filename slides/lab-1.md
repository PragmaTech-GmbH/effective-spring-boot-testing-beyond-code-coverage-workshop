---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Effective Spring Boot Testing Beyond Code Coverage

## Lab 1 — Reliable Integration Tests

Philip Riecks — [PragmaTech GmbH](https://pragmatech.digital/) — [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Effective Spring Boot Testing Beyond Code Coverage' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

# Workshop Goals

- Engineering **confidence** in our development workflow
- Move beyond *coverage* — write tests that catch real bugs
- Tame slow, flaky integration tests
- Tooling: Testcontainers, WireMock, PIT, ArchUnit

---

## Agenda — One Day, Four Labs

1. **Reliable Integration Tests** — Testcontainers, infra deps, Mailpit
2. **Two Modes of `@SpringBootTest`** — WireMock + OAuth2/JWT
3. **Fast & Reliable Builds** — Context caching + parallel execution
4. **Testing Tips 101** — Mutation testing, ArchUnit, tricks & Q&A

---

## Our Sample Application — Library Management System

- Spring Boot 4 / Java 21
- CRUD for **books** (Postgres + Flyway + JPA)
- **OAuth2 Resource Server** (Keycloak in prod, JWT in tests)
- Calls **OpenLibrary** to enrich book metadata on creation
- **Sends an email** via Spring Mail when a book is **deleted**
- Publishes domain events on creation

![w:720](assets/lab-1-sample-application.png)

---

## Two Modes of Testing in Spring Boot

|                  | No context           | With context              |
|------------------|----------------------|----------------------------|
| **Speed**        | Fast (ms)            | Slow (seconds)             |
| **Isolation**    | Pure unit            | Beans + config wired       |
| **Confidence**   | Logic only           | Wiring, JPA, security…     |
| **Tools**        | JUnit + Mockito      | `@SpringBootTest`, slices  |

We focus on **integration tests** for the rest of the day.

---

# Lab 1

## Reliable Integration Tests with Testcontainers

---

## The Problem: A Full `@SpringBootTest` Won't Even Start

```java
@SpringBootTest
class Lab1ApplicationIT {
  @Test void contextLoads() {}
}
```

Fails because the app needs:

- A **PostgreSQL** database
- An **SMTP** server (Mailpit) for the deletion notification
- An **OAuth2 issuer** (Keycloak) to validate JWTs

Mocking all of these is fragile and unrealistic.

---

## Enter Testcontainers

> *"Throwaway, Docker-backed instances of real services for integration tests."*

- Real Postgres, real SMTP, real Keycloak — same versions as prod
- Lifecycle tied to the test JVM
- Spring Boot first-class integration via `@ServiceConnection`

---

## Testcontainers 101

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");
```

`@ServiceConnection` (Spring Boot 3.1+) wires `spring.datasource.*` automatically — no `@DynamicPropertySource` boilerplate.

---

## Dynamic Properties — When You Still Need Them

```java
@DynamicPropertySource
static void mailProps(DynamicPropertyRegistry registry) {
  registry.add("spring.mail.host", mailpit::getHost);
  registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
}
```

Use this for containers without a `@ServiceConnection` factory (yet).

---

## Mailpit as a Test SMTP Server

```java
static GenericContainer<?> mailpit =
    new GenericContainer<>("axllent/mailpit:v1.20")
        .withExposedPorts(1025, 8025)
        .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
        .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");
```

- Port `1025` — SMTP receiver
- Port `8025` — HTTP API to **assert** received emails

---

## End-to-End: Asserting the Deletion Email

```java
mockMvc.perform(delete("/api/books/{id}", id).with(jwt()))
       .andExpect(status().isNoContent());

await().untilAsserted(() ->
  assertThat(mailpitClient.messages())
    .anySatisfy(m -> assertThat(m.subject())
      .startsWith("Book removed from library")));
```

---

## Container Lifecycle Strategies

- **Per-test class** — `@Testcontainers` + non-static `@Container`
- **Per-JVM (singleton)** — `static` field, manual `start()`
- **Spring-managed** — `@ServiceConnection` + `@Bean` in a `@TestConfiguration`

We'll revisit performance trade-offs in **Lab 3**.

---

## Local Dev with Testcontainers

`spring-boot-docker-compose` and `SpringApplication.from(...).with(TestcontainersConfig.class)` let you boot the app **locally** against the same containers your tests use.

→ Same setup for `mvnw spring-boot:test-run` and the IDE debugger.

---

# Time For Some Exercises

See `labs/lab-1/README.md`.

1. Make `Lab1ApplicationIT` start with Postgres + Mailpit + Keycloak
2. Verify the deletion email arrives in Mailpit
3. Boot the app locally against the same containers

---

## Recap

- `@SpringBootTest` needs **real** infra to be meaningful
- Testcontainers + `@ServiceConnection` removes 90% of the boilerplate
- Add Mailpit for SMTP, Keycloak for OAuth2
- Same containers power local dev *and* tests

**Next:** WireMock + OAuth2 — testing the HTTP boundary.
