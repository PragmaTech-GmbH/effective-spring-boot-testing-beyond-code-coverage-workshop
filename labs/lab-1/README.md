# Lab 1 - Writing Reliable Integration Tests Part I

Goal: boot a full `@SpringBootTest` of our Library Management System using Testcontainers for **Postgres**, **Keycloak** (OAuth2), and **Mailpit** (SMTP).

## Learning Objectives

- Boot a real Spring Boot application context against real infrastructure in tests
- Use Testcontainers to manage Docker containers for Postgres, Keycloak, and Mailpit
- Connect containers to Spring via `@ServiceConnection` and `@DynamicPropertySource`
- Write an end-to-end integration test covering HTTP, security, persistence, and email delivery

## Getting Started: Explore the App Locally

Before writing any tests, take a few minutes to explore the running application so you understand what we are testing.

1. Start the app with all infrastructure containers:

```bash
cd labs/lab-1
./mvnw spring-boot:run
```

This uses Spring Boot's Docker Compoe integration to automatically start **Postgres**, **Keycloak**, and **Mailpit** in Docker — no manual setup needed.

2. Open the app in your browser: <http://localhost:8080/app>

3. Log in with **admin** / **admin** (the pre-seeded Keycloak test user)

4. Create a book using a sample ISBN, e.g. `978-0201616224` — the app will call the OpenLibrary API to enrich the title, author, and cover image

5. Browse the book list, open a book's detail page, and try deleting it — watch the deletion notification email arrive in Mailpit at <http://localhost:8025>

Now you have a mental model of the full flow: **create → enrich from OpenLibrary → persist in Postgres → delete → send email via Mailpit**. The exercises below will test exactly this flow.

## Exercises

### Exercise 1: `ExerciseDeleteBookSendsEmailIT`

Write an integration test that deletes a book and verifies the full flow:

1. Start three Testcontainers: PostgreSQL (`@ServiceConnection`), Keycloak, and Mailpit
2. Seed a book into the database
3. Send `DELETE /api/books/{id}` with a valid Bearer token from Keycloak
4. Assert the response is `204 No Content` and the book is gone from the database
5. **Bonus:** Poll Mailpit's REST API to verify the deletion notification email arrived

**Hints:**
- Look at the `experiment` package for `KeycloakContainer` and `MailpitContainer` helpers
- Use `@AutoConfigureRestTestClient` and `RestTestClient` for HTTP requests
- Keycloak provides tokens via `KEYCLOAK.getAccessToken("admin", "admin")`
- For the email assertion, use `Awaitility.await()` since delivery is asynchronous

## How to Run

```bash
cd labs/lab-1
./mvnw verify
```

Mailpit UI: <http://localhost:8025> when running via `./mvnw spring-boot:run`.

## Reference Solutions

Solutions live under `src/test/java/.../lab1/solutions/`:

- `SolutionDeleteBookSendsEmailIT` — full DELETE + Mailpit email assertion via Awaitility
