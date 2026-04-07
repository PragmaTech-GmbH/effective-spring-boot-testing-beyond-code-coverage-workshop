package pragmatech.digital.workshops.lab3.solutions;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * One single context configuration for every integration test in this package.
 *
 * Subclassing this base class is the simplest possible cure for context-cache
 * fragmentation: every subclass shares the exact same {@code @SpringBootTest}
 * configuration, the same Postgres container, and therefore the same cached
 * {@code ApplicationContext}.
 *
 * Singleton {@code static} container = one Postgres per JVM.
 */
@SpringBootTest
public abstract class SharedIntegrationTestBase {

  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }
}
