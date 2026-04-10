package pragmatech.digital.workshops.lab3;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Testcontainers configuration for running {@code Lab3Application} locally via
 * {@code spring-boot:test-run} — not used by integration tests, which get their
 * infrastructure from {@code support.AbstractIntegrationTest}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainerConfig {

  @Bean
  @ServiceConnection
  static PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test")
      .withInitScript("init-postgres.sql");
  }

  /**
   * Placeholder {@link JwtDecoder} so the OAuth2 resource server can start
   * without a real issuer configured. Never invoked during local dev unless
   * someone actually hits a secured endpoint — in which case it throws and
   * tells them to configure a real decoder.
   */
  @Bean
  JwtDecoder jwtDecoder() {
    return token -> {
      throw new UnsupportedOperationException(
        "Placeholder JwtDecoder — configure spring.security.oauth2.resourceserver.jwt.issuer-uri for local dev");
    };
  }
}
