package pragmatech.digital.workshops.lab2.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Singleton Keycloak container for the test suite.
 *
 * <p>Imports {@code keycloak/test-realm.json} on startup, which provisions:
 * <ul>
 *   <li>Realm {@code test-realm}</li>
 *   <li>Confidential client {@code test-client} (secret {@code test-secret}) with
 *       {@code client_credentials} and password grants enabled, default scopes
 *       {@code books:read} and {@code books:write}.</li>
 *   <li>Three users: {@code alice/alice} (books:read), {@code bob/bob}
 *       (books:write), {@code admin/admin} (both).</li>
 * </ul>
 *
 * <p>Use {@code issuerUri()} to wire {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}.
 */
public final class KeycloakTestcontainer {

  public static final String REALM = "test-realm";
  public static final String CLIENT_ID = "test-client";
  public static final String CLIENT_SECRET = "test-secret";

  public static final GenericContainer<?> INSTANCE =
    new GenericContainer<>("quay.io/keycloak/keycloak:26.0")
      .withExposedPorts(8080)
      .withEnv("KEYCLOAK_ADMIN", "admin")
      .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
      .withEnv("KC_HEALTH_ENABLED", "true")
      .withCopyFileToContainer(
        MountableFile.forClasspathResource("keycloak/test-realm.json"),
        "/opt/keycloak/data/import/test-realm.json")
      .withCommand("start-dev", "--import-realm")
      .waitingFor(Wait.forHttp("/realms/" + REALM + "/.well-known/openid-configuration").forPort(8080));

  static {
    INSTANCE.start();
  }

  private KeycloakTestcontainer() {
  }

  public static String issuerUri() {
    return "http://" + INSTANCE.getHost() + ":" + INSTANCE.getMappedPort(8080) + "/realms/" + REALM;
  }

  public static String tokenUri() {
    return issuerUri() + "/protocol/openid-connect/token";
  }
}
