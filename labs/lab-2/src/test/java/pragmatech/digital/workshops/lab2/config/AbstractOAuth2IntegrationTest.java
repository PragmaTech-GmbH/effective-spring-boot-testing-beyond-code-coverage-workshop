package pragmatech.digital.workshops.lab2.config;

import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for integration tests that need a real OAuth2 access token signed by
 * the Keycloak Testcontainer.
 *
 * <p>Two helpers are provided:
 * <ul>
 *   <li>{@link #fetchClientCredentialsToken()} — service-to-service token (the
 *       client itself), carries scopes {@code books:read books:write}.</li>
 *   <li>{@link #fetchPasswordGrantToken(String, String)} — token for one of the
 *       seeded users; the granted authorities reflect the user's realm roles.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractOAuth2IntegrationTest {

  @ServiceConnection
  protected static final PostgreSQLContainer POSTGRES =
    new PostgreSQLContainer("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void resourceServerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakTestcontainer::issuerUri);
  }

  protected static String fetchClientCredentialsToken() {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("client_id", KeycloakTestcontainer.CLIENT_ID);
    body.add("client_secret", KeycloakTestcontainer.CLIENT_SECRET);
    return postForToken(body);
  }

  protected static String fetchPasswordGrantToken(String username, String password) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", KeycloakTestcontainer.CLIENT_ID);
    body.add("client_secret", KeycloakTestcontainer.CLIENT_SECRET);
    body.add("username", username);
    body.add("password", password);
    return postForToken(body);
  }

  @SuppressWarnings("unchecked")
  private static String postForToken(MultiValueMap<String, String> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    Map<String, Object> response = RestClient.create()
      .post()
      .uri(KeycloakTestcontainer.tokenUri())
      .headers(h -> h.addAll(headers))
      .body(new HttpEntity<>(body, headers))
      .retrieve()
      .body(Map.class);

    if (response == null || response.get("access_token") == null) {
      throw new IllegalStateException("Keycloak returned no access_token: " + response);
    }
    return (String) response.get("access_token");
  }

  protected static HttpHeaders bearerHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
