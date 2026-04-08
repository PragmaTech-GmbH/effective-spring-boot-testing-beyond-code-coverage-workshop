package pragmatech.digital.workshops.lab1;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Local development Testcontainers configuration.
 *
 * <p>Start the application with this configuration (via {@code TestLab1Application}) to run the
 * full backing stack — Postgres and a preconfigured Keycloak — inside Docker without having to
 * touch {@code compose.yaml}. The audience can see how Testcontainers powers local dev, not just
 * integration tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainersConfig {

  private static final String KEYCLOAK_REALM = "test-realm";

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test")
      .withInitScript("init-postgres.sql");
  }

  @Bean
  GenericContainer<?> keycloak() {
    return new GenericContainer<>("quay.io/keycloak/keycloak:26.0")
      .withExposedPorts(8080)
      .withEnv("KEYCLOAK_ADMIN", "admin")
      .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
      .withEnv("KC_HEALTH_ENABLED", "true")
      .withCopyFileToContainer(
        MountableFile.forClasspathResource("keycloak/test-realm.json"),
        "/opt/keycloak/data/import/test-realm.json")
      .withCommand("start-dev", "--import-realm")
      .waitingFor(Wait.forHttp("/realms/" + KEYCLOAK_REALM + "/.well-known/openid-configuration").forPort(8080));
  }

  @Bean
  DynamicPropertyRegistrar keycloakProperties(GenericContainer<?> keycloak) {
    return registry -> registry.add(
      "spring.security.oauth2.resourceserver.jwt.issuer-uri",
      () -> "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/" + KEYCLOAK_REALM);
  }
}
