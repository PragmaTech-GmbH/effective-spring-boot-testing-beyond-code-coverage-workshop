package pragmatech.digital.workshops.lab3.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 1 — Same configuration, no cache killers.
 *
 * Compared to {@code Exercise1ContextCacheKillersIT} we removed:
 * <ul>
 *   <li>{@code @MockitoBean} (creates a unique cache key per replaced bean)</li>
 *   <li>{@code @TestPropertySource} with a per-class value</li>
 *   <li>{@code @DirtiesContext} (the worst offender — evicts the cache outright)</li>
 * </ul>
 * Every other test in this package extends {@link SharedIntegrationTestBase} so
 * Spring's {@code TestContextManager} reuses the same context across all of them.
 */
class Solution1ContextCacheKillersIT extends SharedIntegrationTestBase {

  @Test
  void shouldReuseTheCachedContext() {
    assertThat(POSTGRES.isRunning()).isTrue();
  }
}
