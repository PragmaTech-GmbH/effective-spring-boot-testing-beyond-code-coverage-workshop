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

- Testcontainers usage
- Explain Testcontainers 101
- Their default usage for databases
- Overriding dynamic properties
- Use Maiplit additionally

- Effective WireMock usage
- Proxying
- Response templates
- Showcasing stateful examples


### Lab 2: Two Modes of Spring Boot Testing:

- Discuss exercises
- Context Caching
- When to pick which test? 
- No Context vs. Context -> Full Context vs. Sliced Context -> Full context with or w/o servlet environment
- Implications to behaviour and performance
- Show both modes in action


### Lab 3: Fast Builds & Reliable Builds:

- Discuss exercises
- Context Caching explained
- Context Caching Tooling
- Mutation Testing
- Parallel Builds
- Setup and example

### Lab 4: Testing Tips 101:

- Discuss exercises
- General Tips & Tricks for Maven, Pipelines and setup
- Tooling
- Q&A from the audience
