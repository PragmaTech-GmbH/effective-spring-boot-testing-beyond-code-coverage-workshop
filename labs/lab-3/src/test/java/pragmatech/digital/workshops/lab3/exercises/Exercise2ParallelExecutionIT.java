package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 2 — Enable parallel test execution and measure the speedup.
 *
 * Goal:
 * <ol>
 *   <li>Pick one of the two parallelization strategies:
 *     <ul>
 *       <li><b>Maven {@code forkCount=1C}</b> — N JVMs, each with its own context cache.</li>
 *       <li><b>JUnit Jupiter parallel</b> — multiple threads in one JVM, sharing the cache.</li>
 *     </ul>
 *   </li>
 *   <li>Configure your choice (Surefire/Failsafe XML for option 1,
 *       {@code junit-platform.properties} for option 2).</li>
 *   <li>Re-run {@code ./mvnw verify} and compare wall-clock to the baseline.</li>
 *   <li>Find any test that breaks under parallel execution and fix it (don't reach
 *       for {@code @DirtiesContext} or {@code @Execution(SAME_THREAD)} as a
 *       shortcut — fix the underlying race).</li>
 * </ol>
 */
@SpringBootTest
@Disabled("TODO: enable parallel execution and ensure this test stays green")
class Exercise2ParallelExecutionIT {

  @Test
  void shouldRunSafelyAlongsideOtherIntegrationTests() {
    // TODO
  }
}
