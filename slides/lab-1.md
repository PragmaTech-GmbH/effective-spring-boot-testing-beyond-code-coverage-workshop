---
marp: true
theme: pragmatech
---

![bg](./assets/barcelona-spring-io.jpg)

---

<!-- _class: title -->
![bg left:33%](assets/workshop-banner.jpg)

# Effective Spring Boot Testing Beyond Code Coverage

## Full-Day Workshop

_Spring I/O Conference Workshop 13.04.2026_

Philip Riecks | [PragmaTech GmbH](https://pragmatech.digital/) | [@rieckpil](https://x.com/rieckpil)

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
- 11:05 - 13:00: **Lab 2 - Writing Reliable Spring Boot Integration Tests Part II**
- 13:00 - 14:00 **Lunch** (60 minutes)
- 14:00 - 15:30: **Lab 3 - Accelerating Spring Boot Build Times**
- 15:30 - 15:50 **Coffee Break** (20 minutes)
- 15:50 - 17:00: **Lab 4 - Tips & Tricks beyond Code Coverage**

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

# Move beyond *code coverage* - write tests that give us confidence to ship frequently to production.

---

![bg right:33%](assets/why-test-software.jpg)


# Why Test Software?

---

![bg right:33%](assets/ai-image.jpg)


## The AI Trap: Testing is Your Safety Net

- AI generates the logic, but you inherit the liability. It can write the function; it won't join the post-mortem.
- If the AI wrote the code and the AI wrote the test, you are the only person left to solve the hallucination when the system crashes.
- AI is the accelerator. Your tests are the brakes. You need both to go fast.

AI provides the horsepower, but your test suite provides the steering. Together, they turn **coding fast** into **shipping reliably with confidence**.

---

![bg right:33%](assets/northstar.jpg)


## My Overall Northstar for Automated Testing

Imagine seeing this pull request on a Friday afternoon:

![](assets/northstar-pr.png)

How confident are you to merge this major Spring Boot upgrade and deploy it to production once the pipeline turns green?

Good tests don't just catch bugs - they give you **fast feedback** and **confident deployments**.

---

# Workshop Technical Agenda Revisited

- Test context management in Spring Boot
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

![bg right:33%](assets/101.jpg)

## Quick Spring Boot Testing Recap

- The "Testing Swiss Army Knife"


```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

- Batteries-included for testing by transitively including popular testing libraries: JUnit, Mockito, AssertJ, etc.
- **Maven**: Surefire plugin for unit tests and Failsafe plugin for integration tests
- **Gradle**: Built-in test tasks for unit test and a custom task for integration tests


---

## Spring Boot Test Types

![center h:500 w:1000](assets/decision-tree-testing-en.png)

---

![bg right:33%](assets/slice.jpg)

## Sliced Testing with Spring Boot

Verify specific layers of your Spring Boot application with a minimal `ApplicationContext`.

---

![center h:600 w:700](assets/typical-context.png)

---

![center h:600 w:700](assets/typical-context-colored.png)

---


![center h:500 w:600](assets/typical-context-sliced.png)

---


![](assets/typical-context-webmvctest-example.png)


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

## Challenges when Starting the Entire `ApplicationContext`

- **Problem #1**: How to ensure surrounding infrastructure (e.g. database, queues, etc.) is present?
- **Problem #2**: How to handle HTTP communication from our application to remote services?
- **Problem #3**: How to keep our build time at a reasonable duration?

---


## Solving Problem #1 for our database: In-Memory vs. Real Database

- By default, Spring Boot tries to autoconfigure an in-memory relational database (H2 or Derby)
- In-memory database pros:
  - Easy to use & fast
  - Less overhead
- In-memory database cons:
  - Mismatch with the infrastructure setup in production
  - Despite having compatibility modes, we can't fully test proprietary database features

---

![bg right:33%](assets/generated/containers.jpg)

## Testcontainers to the Rescue!

> *"Throwaway, Docker-backed instances of real services for integration tests."*

```java
static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");
```

- Java library that manages **Docker containers** from inside Java code
- [Modules](https://testcontainers.com/modules/) for PostgreSQL, MySQL, Redis, Kafka, LocalStack, and more
- Eliminates the "works on my machine" database problem

---

## Testcontainers 101

- Running infrastructure components (databases, message brokers, etc.) in Docker containers for our tests becomes a breeze with [Testcontainers](https://testcontainers.com/)
- Testcontainers abstracts the rather low-level Docker Java API and provides a fluent, Java-friendly API to define and manage containers in our tests
- Whatever you can containerize, you can test with Testcontainers

Testcontainers maps the container's internal ports to random ephemeral ports on the host machine to avoid conflicts.

You can see the mapped ports with `docker ps`:

```shell {3}
$ docker ps
CONTAINER ID   IMAGE                        COMMAND                  CREATED          STATUS         PORTS                                           NAMES
a958ee2887c6   postgres:16-alpine           "docker-entrypoint.s…"   10 seconds ago   Up 9 seconds   0.0.0.0:32776->5432/tcp, [::]:32776->5432/tcp   affectionate_cannon
ad0f804068dc   testcontainers/ryuk:0.12.0   "/bin/ryuk"              10 seconds ago   Up 9 seconds   0.0.0.0:32775->8080/tcp, [::]:32775->8080/tcp   testcontainers-ryuk-1f9f76a6-46d4-4e19-85c1-e8364da12804
```

---

## Testcontainers & Spring Boot Integration

```java {2,5,6}
@SpringBootTest
@Testcontainers
class ApplicationIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16-alpine");

}
```

- `@Testcontainers` hooks the container into the JUnit Jupiter extension lifecycle
- `@ServiceConnection` reads host/port from the running container and **auto-configures** Spring's datasource - no manual URL wiring needed

When/how to start containers properly? We'll see that in **lab3**.

---

## Alternative Connection Configuration

Alternatively, we can use `@DynamicPropertySource` to programmatically set properties from the container:

```java
static PostgreSQLContainer database =
  new PostgreSQLContainer("postgres:16-alpine")
    .withDatabaseName("test")
    .withUsername("duke")
    .withPassword("s3cret");

@DynamicPropertySource
static void properties(DynamicPropertyRegistry registry) {
  registry.add("spring.datasource.url", database::getJdbcUrl);
  registry.add("spring.datasource.password", database::getPassword);
  registry.add("spring.datasource.username", database::getUsername);
}
```

---

## Mailpit as a Test SMTP Server

```java
static GenericContainer<?> mailpit =
    new GenericContainer<>("axllent/mailpit:v1.20")
        .withExposedPorts(1025, 8025)
        .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
        .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");
```

- Port `1025` - SMTP receiver
- Port `8025` - HTTP UI Inbox of received emails

```java
@DynamicPropertySource
static void mailProps(DynamicPropertyRegistry registry) {
  registry.add("spring.mail.host", mailpit::getHost);
  registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
}
```

---

## What About Keycloak?

**Keycloak** is an open-source Identity Provider (IdP) that handles OAuth2/OIDC authentication and authorization. If your organization uses Azure AD, Okta, Auth0, or AWS Cognito - Keycloak fills the same role. 

We use it here because we can run it as a Docker container with zero cloud dependencies.

---

## Simplified OAuth2 Flow for Our Application

![center](assets/oauth2-flow-simplifed.png)

---

## Preparing Keycloak


**Our test realm setup:**

- A pre-configured **realm** (`workshop`) exported as JSON and auto-imported on container startup
- A **confidential client** (`bookshelf-spa`) that our Spring Boot resource server validates tokens against
- A test **user** (`admin` / `admin`) with the `books:read` and `books:write` scopes we need for our secured endpoints
- The realm export lives in `src/test/resources/` - Keycloak imports it on boot, giving us a fully seeded IdP in seconds

---

# Time For Some Exercises
## Lab 1

See `labs/lab-1/README.md`.

- Set up the [repository](https://github.com/PragmaTech-GmbH/effective-spring-boot-testing-beyond-code-coverage-workshop) locally
- Work locally or use GitHub Codespaces (120 hours/month free)
- Fore Codespaces, pick at least 4-Cores (16 GB RAM) and region `Europe West`
- Navigate to the `labs/lab-1` folder in the repository and complete the tasks as described in the `README` file of that folder
- Time boxed until the end of the coffee break (11:05 AM)
