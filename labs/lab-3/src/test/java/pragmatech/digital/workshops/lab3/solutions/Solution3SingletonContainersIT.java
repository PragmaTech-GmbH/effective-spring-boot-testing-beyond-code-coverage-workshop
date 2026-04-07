package pragmatech.digital.workshops.lab3.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 3 — Singleton container pattern in action.
 *
 * Both this class and every other subclass of {@link SharedIntegrationTestBase}
 * share the very same {@code POSTGRES} field — there is exactly one Postgres
 * container per JVM. Combined with Solution 1 (one shared context) and Solution 2
 * (parallel execution) you get a fast, reliable, deterministic suite.
 *
 * Important rules:
 * <ul>
 *   <li>Never call {@code POSTGRES.stop()} — let JVM exit do the cleanup.</li>
 *   <li>Never replace the field per subclass — that would create N containers.</li>
 *   <li>Run {@code docker ps} mid-build to confirm only one container exists.</li>
 * </ul>
 */
class Solution3SingletonContainersIT extends SharedIntegrationTestBase {

  @Test
  void shouldShareTheSamePostgresAcrossAllTests() {
    assertThat(POSTGRES.isRunning()).isTrue();
    assertThat(POSTGRES.getJdbcUrl()).startsWith("jdbc:postgresql://");
  }
}
