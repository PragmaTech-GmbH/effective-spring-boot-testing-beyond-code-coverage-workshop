# Effective Spring Boot Testing Beyond Code Coverage

Goal:  Engineering Confidence in Our Development Workflow

Take your Spring Boot testing skills beyond the basics. This hands-on workshop covers advanced techniques for writing reliable, fast, and maintainable tests. You'll learn how to leverage Testcontainers for realistic database and service testing, optimize test execution through strategic context caching, and build robust tests for external service dependencies. We'll also explore mutation testing to verify your tests actually catch bugs - not just achieve code coverage. Walk away with practical patterns you can apply to your projects immediately.

Agenda

* Test slices and context management in Spring Boot
* Testcontainers: setup, configuration, and best practices
* Context caching strategies for faster test suites
* Testing external services: WireMock, contract testing, and resilience verification
* Mutation testing with PIT: measuring real test effectiveness
* Performance optimization and test organization patterns

Goals

* Confidently use Testcontainers for database and infrastructure testing
* Understand and optimize Spring context caching behavior
* Apply proven strategies for testing external service integrations
* Use mutation testing to identify weak spots in your test suite
* Reduce test execution time without sacrificing quality

Target Audience

Java developers with basic Spring Boot testing experience who want to write more comprehensive and effective tests. Familiarity with JUnit 5 and Spring Boot fundamentals is expected.


Agenda is same like last year

## Lab Overview

- Introduction
- Goals
- Agenda

### Lab 1: Reliable Integration Tests:

- Recap the two tests with can write with Spring Boot: with a context and without a context
- Unit testingvs sliced and integration testing
- Show @SpringBootTest, context start will fail as our application as multiple dependencies
- Postgres, SMTP and Keycloak
- Testcontainers usage
- Show our sample application: Library Management System to CRUD for books, Email notification on new books, OAuth2 Resource Server
- Explain Testcontainers 101
- Their default usage for databases
- Overriding dynamic properties
- Use Maiplit additionally

Exercises:
- Start/stop containers for IT purposes, use different approaches
- Verify local debug with Spring Debugger and Testcontainers

### Lab 2: Two Modes of Spring Boot Testing:


- Discuss exercises
- Solving next problem: HTTP calls, during the creation of a book we enrich metadata by calling an external service, we want to test this integration but we don't want to start the whole context
- Introduce WireMock
- Effective WireMock usage
- How to provide a valid JWT token for an integration test
- Proxying
- Response templates
- Showcasing stateful examples
- Explain the two modes of @SpringBootTest in detail

Exercise:

- Write a full integration test to CREATE a book (student needs to consider WireMock stubbing)

### Lab 3: Fast Builds & Reliable Builds:

- Discuss exercises
- Now moving on, writing too much integration tests will slow us down, we want to write fast tests, but we also want to have reliable tests, how do we achieve that?
- Context Caching explained
- Context Caching Tooling
- - Context Caching
- When to pick which test?
- No Context vs. Context -> Full Context vs. Sliced Context -> Full context with or w/o servlet environment
- Implications to behaviour and performance
- Show both modes in action
- Parallel Builds
- Setup and example

Exercise:

- Optimize a bad IT test suite for context caching and collect statistics on existing caching behavior
- Optimize the test suite for parallel execution and collect statistics on existing parallel execution behavior

### Lab 4: Testing Tips 101:

- Discuss exercises
- Mutation Testing
- General Tips & Tricks for Maven, Pipelines and setup
- Tooling
- Q&A from the audience
