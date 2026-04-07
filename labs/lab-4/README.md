# Lab 4 — Testing Tips 101

The grab-bag lab. Same baseline app, now used as a target for the techniques.

## Learning Objectives

- Run **mutation testing** with PIT and read the report
- Enforce architecture rules with **ArchUnit**
- Capture log output with `OutputCaptureExtension`
- Use `@RecordApplicationEvents` and `ApplicationContextRunner`
- Categorise tests with JUnit tags for CI/CD
- Know which libraries to reach for: GreenMail/Mailpit, Selenide, Gatling, JMH

## Exercises

1. Run PIT against `BookNotificationService` (the new deletion email path) and inspect surviving mutants.
2. Add an ArchUnit rule that forbids `controller` packages from importing `repository` packages.
3. Use `@RecordApplicationEvents` to assert that `BookCreatedEvent` is published exactly once on creation.
4. Tag a slow IT with `@Tag("slow")` and exclude it from the PR profile.

## How to Run

```bash
./mvnw verify
./mvnw -DwithHistory test-compile org.pitest:pitest-maven:mutationCoverage
open target/pit-reports/index.html
```
