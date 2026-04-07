package pragmatech.digital.workshops.lab4.experiment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab4.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab4.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab4.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates a "nightly" tagged integration test.
 *
 * <p>This test is expensive because it starts a full Spring Boot application context
 * with Testcontainers (PostgreSQL) and WireMock. Tests like this are good candidates
 * for nightly builds rather than running on every push.
 *
 * <p>Usage:
 * <pre>
 *   # Run only nightly-tagged tests
 *   ./mvnw verify -Dgroups=nightly
 *
 *   # Exclude nightly tests from regular builds
 *   ./mvnw verify -DexcludedGroups=nightly
 * </pre>
 *
 * <p>In a real project, nightly tests might include:
 * <ul>
 *   <li>Performance/load tests</li>
 *   <li>Full end-to-end scenarios with many services</li>
 *   <li>Data migration tests</li>
 *   <li>Browser-based UI tests</li>
 *   <li>Tests against external staging environments</li>
 * </ul>
 */
@Tag("nightly")
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class NightlyBuildDemoIT {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldRunExpensiveEndToEndTest() {
    // This is an expensive test that starts the full application context.
    // It should only run in nightly builds, not on every push.
    // Run with: ./mvnw verify -Dgroups=nightly
    assertThat(bookRepository).isNotNull();
    System.out.println("Nightly build test executed -- full Spring context with PostgreSQL and WireMock");
  }

  @Test
  void shouldVerifyDatabaseConnectivity() {
    // Another example of an expensive integration test.
    // Verifying that the repository can interact with the database.
    long bookCount = bookRepository.count();
    assertThat(bookCount).isGreaterThanOrEqualTo(0);
    System.out.println("Database connectivity verified -- found " + bookCount + " books");
  }
}
