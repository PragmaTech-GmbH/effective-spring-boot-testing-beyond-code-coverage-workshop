package pragmatech.digital.workshops.lab2.experiment;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates how to capture and inspect Testcontainers logs.
 *
 * <p>To add Slf4jLogConsumer to LocalDevTestcontainerConfig:
 * <pre>
 * {@code
 * @Bean
 * @ServiceConnection
 * static PostgreSQLContainer<?> postgres() {
 *   return new PostgreSQLContainer<>("postgres:16-alpine")
 *     .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("postgres")));
 * }
 * }
 * </pre>
 */
@Testcontainers
class ContainerLogsTest {

  private static final Logger log = LoggerFactory.getLogger(ContainerLogsTest.class);

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withLogConsumer(new Slf4jLogConsumer(log));

  @Test
  void shouldCapturePostgresStartupLogsWhenContainerStarts() {
    assertThat(postgres.getLogs())
      .contains("database system is ready to accept connections");
  }
}
