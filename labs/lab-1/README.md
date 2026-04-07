# Lab 1 — Reliable Integration Tests

Goal: get a full `@SpringBootTest` of the Library Management System green using Testcontainers for **Postgres**, **Mailpit** (SMTP), and **Keycloak** (OAuth2).

The application has gained one new feature compared to the workshop baseline: **when a book is deleted, an email is sent via Spring Mail** (`BookService.deleteBook` → `BookNotificationService.notifyDeletedBook`). You will exercise this end-to-end across all four labs.

## Learning Objectives

- Boot a real Spring Boot application context against real infra in tests
- Use `@ServiceConnection` and `@DynamicPropertySource`
- Add a `GenericContainer` for Mailpit and assert delivered emails through its HTTP API
- Run the same containers locally for debugging

## Exercises

1. Make `Lab1ApplicationIT` start with Postgres, Mailpit and Keycloak (Testcontainers).
2. Write an integration test that `DELETE`s a book and asserts the email lands in Mailpit (poll `http://<mailpit-host>:8025/api/v1/messages`).
3. Use `SpringApplication.from(...).with(...)` to boot the app locally against the same containers and step through the delete flow with the debugger.

## How to Run

```bash
./mvnw verify
```

Mailpit UI: <http://localhost:8025> when running via `compose.yaml`.
