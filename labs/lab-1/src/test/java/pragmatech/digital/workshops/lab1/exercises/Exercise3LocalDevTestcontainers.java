package pragmatech.digital.workshops.lab1.exercises;

import pragmatech.digital.workshops.lab1.LocalDevTestcontainersConfig;

/**
 * Exercise 3 — Boot the application locally with the same Testcontainers stack.
 *
 * Goal: when you run {@code ./mvnw spring-boot:test-run} (or run the
 * {@code TestLab1Application} class from your IDE), the application should come up
 * against fresh Postgres + Mailpit + WireMock containers, with no manual
 * {@code docker compose up}.
 *
 * Steps:
 * <ol>
 *   <li>Add a {@code TestLab1Application} class under
 *       {@code src/test/java/.../lab1/} with a {@code main} method that calls
 *       {@code SpringApplication.from(Lab1Application::main).with(LocalDevTestcontainerConfig.class).run(args)}.</li>
 *   <li>Extend {@link LocalDevTestcontainersConfig}
 *       so it also exposes a Mailpit container as a {@code @Bean} (and any other
 *       services you need).</li>
 *   <li>Place a breakpoint inside {@code BookService.deleteBook} and step through a
 *       real DELETE request via the {@code bookshelf.http} HTTP file.</li>
 * </ol>
 *
 * There is no test class for this exercise — verification is manual: hit the running
 * app with {@code DELETE /api/books/{id}} and confirm the email shows up at
 * {@code http://localhost:8025} (the Mailpit web UI).
 */
class Exercise3LocalDevTestcontainers {
}
