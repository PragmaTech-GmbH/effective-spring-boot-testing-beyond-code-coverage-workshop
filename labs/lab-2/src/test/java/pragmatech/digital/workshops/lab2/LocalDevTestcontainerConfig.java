package pragmatech.digital.workshops.lab2;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;

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
   * Minimal {@link JwtDecoder} so {@code SecurityConfig} can wire up the OAuth2
   * resource server without requiring {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
   * to point at a real IdP. This decoder is never invoked by the
   * {@code contextLoads} smoke test — integration tests that actually exercise
   * authentication provide their own decoder or real JWKS stub.
   */
  @Bean
  JwtDecoder jwtDecoder() {
    return token -> {
      throw new UnsupportedOperationException(
        "Placeholder JwtDecoder — integration tests that hit secured endpoints must provide a real decoder");
    };
  }
}
