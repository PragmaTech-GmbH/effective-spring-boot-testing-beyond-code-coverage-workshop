package pragmatech.digital.workshops.lab1.experiment;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Reusable custom Testcontainer for Keycloak 26.x — use it like {@code PostgreSQLContainer}:
 *
 * <pre>{@code
 * static KeycloakContainer keycloak = new KeycloakContainer();
 *
 * // or fully configured:
 * static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.3")
 *   .withRealmImportFile("keycloak/my-realm.json")
 *   .withRealm("my-realm")
 *   .withAdminCredentials("admin", "admin");
 * }</pre>
 *
 * <p>Defaults import the realm {@code test-realm} from
 * {@code src/test/resources/keycloak/test-realm.json}, which provisions:
 * <ul>
 *   <li>Confidential client {@code test-client} (secret {@code test-secret}) with
 *       client_credentials + password grants and scopes {@code books:read}, {@code books:write}.</li>
 *   <li>Three users: {@code alice/alice} (books:read), {@code bob/bob} (books:write),
 *       {@code admin/admin} (both).</li>
 * </ul>
 *
 * <p>Use {@link #getIssuerUri()} to wire
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}.
 */
public class KeycloakContainer extends GenericContainer<KeycloakContainer> {

  public static final String NAME = "keycloak";

  public static final String IMAGE = "quay.io/keycloak/keycloak";

  public static final String DEFAULT_TAG = "26.3";

  public static final Integer KEYCLOAK_HTTP_PORT = 8080;

  public static final String DEFAULT_CLIENT_ID = "test-client";

  public static final String DEFAULT_CLIENT_SECRET = "test-secret";

  static final String DEFAULT_ADMIN_USER = "admin";

  static final String DEFAULT_ADMIN_PASSWORD = "admin";

  static final String DEFAULT_REALM = "test-realm";

  static final String DEFAULT_REALM_IMPORT_FILE = "keycloak/test-realm.json";

  private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE);

  private String realm = DEFAULT_REALM;

  private String realmImportFile = DEFAULT_REALM_IMPORT_FILE;

  private String adminUser = DEFAULT_ADMIN_USER;

  private String adminPassword = DEFAULT_ADMIN_PASSWORD;

  public KeycloakContainer() {
    this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
  }

  public KeycloakContainer(final String dockerImageName) {
    this(DockerImageName.parse(dockerImageName));
  }

  public KeycloakContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    addExposedPort(KEYCLOAK_HTTP_PORT);
    setCommand("start-dev", "--import-realm");
  }

  @Override
  protected void configure() {
    addEnv("KC_BOOTSTRAP_ADMIN_USERNAME", adminUser);
    addEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", adminPassword);
    addEnv("KC_HEALTH_ENABLED", "true");
    this.withCopyFileToContainer(
      MountableFile.forClasspathResource(realmImportFile),
      "/opt/keycloak/data/import/" + realm + "-realm.json");
    this.waitStrategy = Wait.forHttp("/realms/" + realm + "/.well-known/openid-configuration")
      .forPort(KEYCLOAK_HTTP_PORT);
  }

  public KeycloakContainer withRealm(final String realm) {
    this.realm = realm;
    return self();
  }

  public KeycloakContainer withRealmImportFile(final String classpathResource) {
    this.realmImportFile = classpathResource;
    return self();
  }

  public KeycloakContainer withAdminCredentials(final String username, final String password) {
    this.adminUser = username;
    this.adminPassword = password;
    return self();
  }

  public String getRealm() {
    return realm;
  }

  public String getAdminUser() {
    return adminUser;
  }

  public String getAdminPassword() {
    return adminPassword;
  }



  public String getIssuerUri() {
    return "http://" + getHost() + ":" + getMappedPort(KEYCLOAK_HTTP_PORT) + "/realms/" + realm;
  }

  public String getTokenUri() {
    return getIssuerUri() + "/protocol/openid-connect/token";
  }

  /**
   * Fetches a signed JWT access token via the OAuth2 Resource Owner Password Credentials grant
   * against the configured realm's {@code test-client}. Uses the JDK's built-in {@link HttpClient} —
   * no extra dependencies required.
   *
   * @param username realm user (e.g. {@code alice})
   * @param password realm user password
   * @return the raw {@code access_token} value ready to drop into an {@code Authorization: Bearer} header
   */
  public String getAccessToken(final String username, final String password) {
    Map<String, String> formParameters = Map.of(
      "grant_type", "password",
      "client_id", DEFAULT_CLIENT_ID,
      "client_secret", DEFAULT_CLIENT_SECRET,
      "username", username,
      "password", password,
      "scope", "openid books:read books:write"
    );

    String formBody = formParameters.entrySet().stream()
      .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
      .collect(Collectors.joining("&"));

    HttpRequest tokenRequest = HttpRequest.newBuilder()
      .uri(URI.create(getTokenUri()))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Accept", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(formBody))
      .build();

    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> response = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IllegalStateException(
          "Failed to fetch access token for user '" + username + "': HTTP "
            + response.statusCode() + " — " + response.body());
      }

      return extractAccessToken(response.body());
    }
    catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while fetching access token", interruptedException);
    }
    catch (java.io.IOException ioException) {
      throw new IllegalStateException("I/O failure while fetching access token", ioException);
    }
  }

  private static final Pattern ACCESS_TOKEN_PATTERN =
    Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");

  private static String extractAccessToken(final String jsonBody) {
    Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(jsonBody);
    if (!matcher.find()) {
      throw new IllegalStateException("No 'access_token' field in token endpoint response: " + jsonBody);
    }
    return matcher.group(1);
  }
}
