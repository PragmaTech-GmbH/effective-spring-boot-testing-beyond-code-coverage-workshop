package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 3 — Convert a test from {@code MOCK} to {@code RANDOM_PORT} and observe
 * what breaks.
 *
 * Goal:
 * <ol>
 *   <li>Take the test you wrote in Exercise 1 (or copy {@code Lab2ApplicationIT})
 *       and switch it from the default {@code WebEnvironment.MOCK} to
 *       {@code WebEnvironment.RANDOM_PORT}.</li>
 *   <li>Replace {@code MockMvc} with {@code TestRestTemplate} or
 *       {@code WebTestClient}.</li>
 *   <li>If you previously relied on {@code @Transactional} for cleanup, observe
 *       that data is now committed to Postgres and persists across tests.</li>
 *   <li>Fix the cleanup using one of: per-test database isolation, an
 *       {@code @AfterEach} that truncates tables, or a unique random ISBN per
 *       test.</li>
 * </ol>
 *
 * Write down: which mode would you pick for *this* test and why?
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("TODO: implement Exercise 3")
class Exercise3MockVsRandomPortIT {

  @Test
  void shouldStillCleanUpDataWithoutTransactionalRollback() {
    // TODO
  }
}
