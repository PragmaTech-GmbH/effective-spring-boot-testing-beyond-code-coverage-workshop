package pragmatech.digital.workshops.lab1.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 1 — Add a Mailpit Testcontainer to the integration test.
 *
 * The application sends an email via Spring Mail whenever a book is deleted
 * (see {@code BookService#deleteBook} → {@code BookNotificationService#notifyDeletedBook}).
 *
 * Goal:
 * <ol>
 *   <li>Start a Mailpit container (image {@code axllent/mailpit:v1.20}, ports 1025/8025)
 *       alongside the existing Postgres + WireMock setup.</li>
 *   <li>Wire {@code spring.mail.host} and {@code spring.mail.port} via
 *       {@code @DynamicPropertySource} so the Spring context picks up the SMTP server.</li>
 *   <li>Assert the application context loads with the new infra in place.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>Use {@code GenericContainer} — there is no dedicated module for Mailpit.</li>
 *   <li>Set the env vars {@code MP_SMTP_AUTH_ACCEPT_ANY=1} and
 *       {@code MP_SMTP_AUTH_ALLOW_INSECURE=1} so the container accepts connections
 *       without TLS / auth.</li>
 *   <li>The mapped HTTP port (8025) exposes a JSON API at
 *       {@code GET /api/v1/messages} you'll use in Exercise 2.</li>
 * </ul>
 */
@SpringBootTest
@Disabled("TODO: implement Exercise 1 — add a Mailpit container and remove this annotation")
class Exercise1MailpitContainerTest {

  @Test
  void shouldStartContextWithMailpitContainer() {
    // TODO: assert the JavaMailSender bean points at the Mailpit container.
  }
}
