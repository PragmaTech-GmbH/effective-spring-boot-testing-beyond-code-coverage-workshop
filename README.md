# Effective Spring Boot Testing Beyond Code Coverage 🍃

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/PragmaTech-GmbH/effective-spring-boot-testing-beyond-code-coverage-workshop)

A one-day, advanced workshop on writing **fast, reliable, high-signal** tests for Spring Boot applications. Assumes you already know JUnit, Mockito, and the basics of `@SpringBootTest`.

**Location**: Spring I/0 2026, April 13, 09:00 - 17:00

Proudly presented by [PragmaTech GmbH](https://pragmatech.digital/).

## Workshop Overview

Take your Spring Boot testing skills beyond the basics: realistic infrastructure with Testcontainers, the HTTP boundary with WireMock, context caching and parallel execution for speed, and mutation testing to verify your tests actually catch bugs.

All four labs share **one** sample application — a Library Management System (book CRUD, OAuth2 Resource Server, OpenLibrary metadata enrichment, and an email-on-delete notification via Spring Mail).

### Agenda - One Day, Four Labs

| Time          | Session                                                                                |
|---------------|----------------------------------------------------------------------------------------|
| 09:00 - 10:30 | [Lab 1](labs/lab-1) — Reliable Integration Tests with Testcontainers                   |
| 10:30 - 11:00 | Coffee Break                                                                           |
| 11:00 - 12:30 | [Lab 2](labs/lab-2) — Two Modes of `@SpringBootTest`: WireMock + OAuth2/JWT            |
| 12:30 - 13:30 | Lunch                                                                                  |
| 13:30 - 15:00 | [Lab 3](labs/lab-3) — Fast & Reliable Builds: Context Caching + Parallel Execution     |
| 15:00 - 15:30 | Coffee Break                                                                           |
| 15:30 - 17:00 | [Lab 4](labs/lab-4) — Testing Tips 101: Mutation Testing, ArchUnit & Q&A               |

## Lab Structure

Each lab in `labs/` (`lab-1` through `lab-4`) includes:

- Exercise files with instructions and TODO comments
- Solution files that show the complete implementation
- Supporting code and configurations

## Prerequisites

- Java 21 (or later)
- Docker (for Testcontainers)
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

## Getting Started

1. Clone this repository
2. Import the project at the root into your IDE of choice.
3. Run all builds with:

```bash
./mvnw verify
```

## Slides

You'll find the slides for each lab in the `slides/` directory. They are organized by lab and can be used as a reference during the workshop.

## Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Spring Test Documentation](https://docs.spring.io/spring-framework/reference/testing.html)
- [JUnit 6 User Guide](https://docs.junit.org/6.0.3/overview.html)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [WireMock Documentation](http://wiremock.org/docs/)

