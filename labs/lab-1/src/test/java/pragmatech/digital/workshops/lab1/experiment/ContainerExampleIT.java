package pragmatech.digital.workshops.lab1.experiment;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class ContainerExampleIT {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerExampleIT.class);

  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
    .withEnv("FOO_BAR", "42")
    .withLogConsumer(new Slf4jLogConsumer(LOG))
    .withReuse(true);

  static GenericContainer<?> MAILPIT =
    new GenericContainer<>("axllent/mailpit:v1.20")
      .withExposedPorts(1025, 8025)
      .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
      .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1")
      .waitingFor(Wait.forHttp("/readyz").forPort(8025));

  @Test
  void startDatabaseContainer() {
    POSTGRES.start();
    System.out.println(POSTGRES.getJdbcUrl());

    POSTGRES.stop();
  }

  @Test
  void startMailContainer() {
    MAILPIT.start();

    System.out.println("Mailpit UI is accessible at: http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(8025));

    MAILPIT.stop();
  }
}
