package pragmatech.digital.workshops.lab3.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for tests that require a PostgreSQL database.
 * Uses TestContainers to start a PostgreSQL container for integration tests.
 */
@Testcontainers
public abstract class PostgresTestcontainer {

  /**
   * The PostgreSQL container. Using a static field ensures that the container
   * is started only once for all tests.
   */
  @Container
  private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

  static {
    // Start the container before any tests are run
    postgresContainer.start();
  }

  /**
   * Configure Spring to use the TestContainers PostgreSQL instance.
   */
  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
  }
}
