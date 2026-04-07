---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Effective Spring Boot Testing Beyond Code Coverage

## Lab 2 — Two Modes of `@SpringBootTest`

Philip Riecks — [PragmaTech GmbH](https://pragmatech.digital/) — [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Effective Spring Boot Testing Beyond Code Coverage' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 1

- Did your `@SpringBootTest` boot end-to-end?
- How fast was the first run vs. the second run?
- Surprises with port mapping / `@DynamicPropertySource`?

---

# Lab 2

## Two Modes of `@SpringBootTest` — WireMock & Security

---

## The Next Problem: External HTTP Calls

`BookService.createBook` calls **OpenLibrary** to enrich metadata:

```java
BookMetadataResponse metadata = openLibraryApiClient.getBookByIsbn(request.isbn());
```

In tests we want to:

- ❌ Not hit the real internet (slow, flaky, rate-limited)
- ✅ Stay realistic — exercise the actual HTTP client + serialization
- ✅ Control responses, including failures and timeouts

---

## Introducing WireMock

> *"A simulator for HTTP-based APIs."*

- Real HTTP server bound to a random port
- Stub `GET /api/books/...` with a canned JSON body
- **Verify** which requests your code made
- Advanced: response templates, proxying, stateful scenarios

---

## Effective WireMock Usage

```java
@RegisterExtension
static WireMockExtension wm = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

@DynamicPropertySource
static void apiUrl(DynamicPropertyRegistry r) {
  r.add("book.metadata.api.url", wm::baseUrl);
}
```

---

## Stubbing a Response

```java
wm.stubFor(get(urlPathMatching("/api/books/.*"))
  .willReturn(okJson("""
    { "title": "Effective Java", "covers": [12345] }
  """)));
```

Then assert WireMock was actually called:

```java
wm.verify(getRequestedFor(urlPathEqualTo("/api/books/9780134685991")));
```

---

## WireMock — Advanced Features

- **Response templates** — Handlebars over the request body
- **Proxying** — record real responses, replay them
- **Stateful scenarios** — first call fails, second succeeds (resilience tests)
- **Faults** — connection reset, slow response, malformed body

---

## How to Provide a Valid JWT for an Integration Test

Three options:

1. **`SecurityMockMvcRequestPostProcessors.jwt()`** — only with MockMvc (`MOCK` mode)
2. **`@WithMockUser` / `@WithMockJwtAuth`** — annotation-driven
3. **Real signed JWT** — sign with a test key, point `issuer-uri` at WireMock

We'll use option 3 for **`RANDOM_PORT`** integration tests.

---

## OAuth2 Option 3 — Sign a Token Yourself

```java
@TestConfiguration
class TestJwtConfig {
  @Bean JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(testPublicKey).build();
  }
}
```

Then in the test:

```java
String token = TestJwtFactory.signed(Map.of("sub", "alice", "scope", "books:write"));
mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + token));
```

---

## The Two Modes of `@SpringBootTest`

| Mode                                  | Servlet | HTTP | Use with                    |
|---------------------------------------|---------|------|-----------------------------|
| `webEnvironment = MOCK` *(default)*   | Mock    | No   | `MockMvc`                   |
| `webEnvironment = RANDOM_PORT`        | Real    | Yes  | `TestRestTemplate` / `WebTestClient` |

`MOCK` is faster and lets `@Transactional` rollback work.
`RANDOM_PORT` is closer to production — needed for filters, async, real auth flows.

---

## `@Transactional` and `RANDOM_PORT`

⚠️ With `RANDOM_PORT`, the test thread and the request thread are **different**. Rollback in the test thread will **not** roll back data committed by the controller.

Use `JdbcTemplate` cleanups or **Testcontainers + database-per-test** instead.

---

## Customising the Test Context

- `@TestConfiguration` — add/replace beans for one test
- `@MockitoBean` — replace a bean with a Mockito mock (Spring Boot 3.4+)
- `@ActiveProfiles("test")` — switch profiles
- `@DynamicPropertySource` — late-bound properties from containers

---

# Time For Some Exercises

See `labs/lab-2/README.md`.

1. Write a full integration test for `POST /api/books` with WireMock stubbing OpenLibrary
2. Provide a valid JWT for the request
3. Pick `MOCK` vs `RANDOM_PORT` — and justify your choice

---

## Recap

- WireMock = your HTTP boundary, controllable and offline
- Three ways to bring auth into a test — pick by mode
- `MOCK` vs `RANDOM_PORT`: faster vs more realistic
- `@Transactional` is **not** a magic cleanup for `RANDOM_PORT`

**Next:** make all this *fast*.
