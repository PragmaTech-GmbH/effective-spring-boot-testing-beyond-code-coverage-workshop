package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 3 — Convert per-class containers to the singleton pattern.
 *
 * Goal:
 * <ol>
 *   <li>Inspect {@code PostgresTestcontainer} and the Mailpit container you added
 *       in Lab 1: today they may be created per test class.</li>
 *   <li>Refactor them into a {@code AbstractIntegrationTest} base class with
 *       {@code static} container fields started in a {@code static} initialiser
 *       block — one container per JVM.</li>
 *   <li>Wire them via {@code @ServiceConnection} (Postgres) and
 *       {@code @DynamicPropertySource} (Mailpit).</li>
 *   <li>Re-run the suite and confirm that {@code docker ps} only shows one Postgres
 *       and one Mailpit container during the test run.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>Do <b>not</b> call {@code container.stop()} — let the JVM exit handle it.</li>
 *   <li>Combined with Exercise 1 (one shared context) and Exercise 2 (parallel),
 *       this is the high-water mark of the day in terms of speedup.</li>
 * </ul>
 */
@SpringBootTest
@Disabled("TODO: implement Exercise 3")
class Exercise3SingletonContainersIT {

  @Test
  void shouldShareTheSamePostgresAndMailpitAcrossAllTests() {
    // TODO
  }
}
