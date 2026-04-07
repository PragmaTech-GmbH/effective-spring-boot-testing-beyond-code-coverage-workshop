package pragmatech.digital.workshops.lab1.solutions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 1 — A {@code @SpringBootTest} that boots Postgres + Mailpit via Testcontainers
 * and verifies the {@code JavaMailSender} bean is wired against the running Mailpit.
 */
@SpringBootTest
@Testcontainers
@ExtendWith({})
class Solution1MailpitContainerTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:v1.20")
    .withExposedPorts(1025, 8025)
    .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
    .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");

  @DynamicPropertySource
  static void mailProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mail.host", mailpit::getHost);
    registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
  }

  @Autowired
  JavaMailSenderImpl javaMailSender;

  @Test
  void shouldStartContextWithMailpitContainer() {
    assertThat(javaMailSender.getHost()).isEqualTo(mailpit.getHost());
    assertThat(javaMailSender.getPort()).isEqualTo(mailpit.getMappedPort(1025));
  }
}
