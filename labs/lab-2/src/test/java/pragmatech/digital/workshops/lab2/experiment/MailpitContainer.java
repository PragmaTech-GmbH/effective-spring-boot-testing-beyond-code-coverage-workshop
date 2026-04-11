package pragmatech.digital.workshops.lab2.experiment;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Reusable custom Testcontainer for Mailpit (a lightweight SMTP server with a web UI and REST API).
 *
 * <p>Instantiate via {@code new MailpitContainer()} in an integration test, like you would a
 * {@code PostgreSQLContainer}. Exposes an SMTP listener for the app under test and an HTTP API
 * that tests can poll to assert on received messages.
 *
 * <p>Typical wiring via {@code @DynamicPropertySource}:
 * <pre>{@code
 * @DynamicPropertySource
 * static void mailProperties(DynamicPropertyRegistry registry) {
 *   registry.add("spring.mail.host", MAILPIT::getHost);
 *   registry.add("spring.mail.port", MAILPIT::getSmtpPort);
 * }
 * }</pre>
 *
 * <p>Use {@link #getApiBaseUrl()} to fetch captured messages (e.g. {@code GET /api/v1/messages}).
 */
public class MailpitContainer extends GenericContainer<MailpitContainer> {

  private static final String IMAGE = "axllent/mailpit:v1.20";
  private static final int SMTP_PORT = 1025;
  private static final int HTTP_PORT = 8025;

  public MailpitContainer() {
    super(IMAGE);
    addExposedPorts(SMTP_PORT, HTTP_PORT);
    addEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1");
    addEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");
    this.waitStrategy = Wait.forHttp("/readyz").forPort(HTTP_PORT);
  }

  public int getSmtpPort() {
    return getMappedPort(SMTP_PORT);
  }

  public int getHttpPort() {
    return getMappedPort(HTTP_PORT);
  }

  public String getApiBaseUrl() {
    return "http://" + getHost() + ":" + getHttpPort();
  }
}
