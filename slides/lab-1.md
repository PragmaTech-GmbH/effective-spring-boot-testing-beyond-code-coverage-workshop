---
marp: true
theme: pragmatech
---

![bg](./assets/barcelona-spring-io.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Effective Spring Boot Testing Beyond Code Coverage


## Full-Day Workshop

_Spring I/O Conference Workshop 13.04.2026_

Philip Riecks — [PragmaTech GmbH](https://pragmatech.digital/) — [@rieckpil](https://x.com/rieckpil)

--- 

<!-- header: 'Effective Spring Boot Testing Beyond Code Coverage @ Spring I/0 2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

# Organization

- Hotel WiFi: `Spring I/O` Password: `bootifulBCN`

- Workshop lab requirements
  - Hardware: A personal or company laptop.
  - Development Environment: A Java IDE of your choice with Java 21
  - Access to a functional Docker Engine compatible with Testcontainers.
  - Fallback Access: A personal GitHub account for GitHub Codespaces if local setup fails.


---


# Workshop Timeline

- 9:00 - 10:45: **Lab 1 - Writing Reliable Spring Boot Integration Tests Part I** 
- 10:45 - 11:05: **Coffee Break** (20 minutes)
- 11:05 - 13:00: **Lab 1 - Writing Reliable Spring Boot Integration Tests Part 2**
- 13:00 - 14:00 **Lunch** (60 minutes)
- 14:00 - 15:30: **Lab 3 - Accelerating Spring Boot Build Times**
- 15:30 - 15:50 **Coffee Break** (20 minutes)
- 15:50 - 17:00: **Lab 4 - Pitfalls & Best Practices, Time for Q&A**

---


![bg right:33% h:750](assets/location.png)

## Workshop Instructor: Philip

- Self-employed IT consultant from Herzogenaurach, Germany (Bavaria) 🍻
- BBlogging & content creation with a focus on testing Java and specifically Spring Boot applications 🍃
- Founder of PragmaTech GmbH - Enabling Developers to Frequently Deliver Software with More Confidence 🚤
- Enjoys writing tests (sometimes even more than production code) 🧪

---

## Getting to Know Each Other

- What's your name and where are you from?
- What's your role in your team?
- What's the biggest Spring Boot testing challenge in your team/organization?
- What's your expectation for this workshop?

---

![bg right:33% h:750](assets/best-practices.jpg)

# Workshop Goals Revisited


- Confidently use Testcontainers for database and infrastructure testing
- Understand and optimize Spring context caching behavior
- Apply proven strategies for testing external service integrations
- Use mutation testing to identify weak spots in your test suite
- Reduce test execution time without sacrificing quality

---

# Move beyond *code coverage* - write tests that give you confidence to ship frequently to production.

---

# Workshop Technical Agenda Revisited

- Test slices and context management in Spring Boot
- Testcontainers: setup, configuration, and best practices 
- Context caching strategies for faster test suites
- Testing external services: WireMock, contract testing, and resilience verification
- Mutation testing with PIT: measuring real test effectiveness
- Performance optimization and test organization patterns

---

## Our Sample Application - Bookshelf

- A sample Library Management System
- Spring Boot 4 / Java 21
- CRUD API for **books** (Postgres + Flyway + JPA)
- **OAuth2 Resource Server** (Keycloak as an identity provider)
- Simple vanilla TypeScript frontend
- Calls the **OpenLibrary** REST API to enrich book metadata on creation
- **Sends an email** via Spring Mail when a book is **deleted**

---

![w:720 center](assets/lab-1-sample-application.png)

---

## Application Setup & Demo

- Go to menti.com and enter code `7854 8520`
- Clone the repository locally
- Open the project at the root inside your IDE
- Each lab has a dedicated folder within `labs/`
- The code that I show during the labs is in the `experiment` package, your tasks in `exercises` and solutions in `solutions`
- Fallback: Use GitHub Codespaces if you have trouble with local setup

---

## Quick Spring Boot Testing Recap




---




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
