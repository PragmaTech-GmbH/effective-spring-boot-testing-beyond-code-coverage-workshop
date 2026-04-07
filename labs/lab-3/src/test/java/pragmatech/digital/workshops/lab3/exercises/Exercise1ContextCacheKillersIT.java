package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;

/**
 * Exercise 1 — Find and remove the context cache killers.
 *
 * The classes {@code Exercise1ContextCacheKillersIT}, {@code KillerATest},
 * {@code KillerBTest} and {@code KillerCTest} all start a fresh
 * {@code ApplicationContext} for completely unnecessary reasons.
 *
 * Goal:
 * <ol>
 *   <li>Run the suite with
 *       {@code -Dlogging.level.org.springframework.test.context.cache=DEBUG} and
 *       count how many distinct contexts get created.</li>
 *   <li>Identify the cache killer in each class — there is at least one in this
 *       file alone (look closely!).</li>
 *   <li>Refactor every test to extend a single {@code SharedIntegrationTestBase}
 *       so that exactly one context is built and reused.</li>
 *   <li>Re-run the suite and report the new context count and the wall-clock
 *       improvement.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>{@code @MockitoBean}, unique {@code @TestPropertySource} values,
 *       {@code @DirtiesContext}, and even differing combinations of {@code @Import}
 *       all create new cache keys.</li>
 * </ul>
 */
@SpringBootTest
@TestPropertySource(properties = "exercise.killer=A")
@DirtiesContext
@Disabled("TODO: fix the cache killers in Exercise 1")
class Exercise1ContextCacheKillersIT {

  @MockitoBean
  OpenLibraryApiClient openLibraryApiClient;

  @Test
  void shouldNotKillTheContextCache() {
    // TODO: remove every reason this test invalidates the cache
  }
}
