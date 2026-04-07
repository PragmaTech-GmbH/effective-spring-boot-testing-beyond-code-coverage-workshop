package pragmatech.digital.workshops.lab1.solutions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import pragmatech.digital.workshops.lab1.Lab1Application;

/**
 * Solution 3 — Boot the application locally against the same Testcontainers stack.
 *
 * Run this class' {@code main} method (right-click → Run in your IDE) and the app
 * starts on port 8080 connected to fresh Postgres + Mailpit containers. The Mailpit
 * web UI is reachable at the host port mapped from 8025.
 */
public class Solution3LocalDevTestcontainers {

  public static void main(String[] args) {
    SpringApplication.from(Lab1Application::main)
      .with(LocalDevContainers.class)
      .run(args);
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class LocalDevContainers {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
      return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Bean
    GenericContainer<?> mailpit() {
      return new GenericContainer<>("axllent/mailpit:v1.20")
        .withExposedPorts(1025, 8025)
        .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
        .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");
    }
  }
}
