package pragmatech.digital.workshops.lab2.experiment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Experiment — a BookController integration test that skips Keycloak entirely.
 *
 * <p>Instead of booting a Keycloak Testcontainer (as {@code AbstractOAuth2IntegrationTest}
 * does), we stand up a {@link WireMockServer} once and let {@link OAuth2Stubs}
 * pretend it is the identity provider: it stubs the OIDC discovery document and
 * the JWKS endpoint with a locally generated RSA key, and mints tokens signed
 * with the matching private key.
 *
 * <p>Why this is interesting:
 * <ul>
 *   <li>No Docker pull, no ~10s Keycloak startup — tests run in under a second.</li>
 *   <li>Fully hermetic: no coupling to a specific Keycloak version or realm seed.</li>
 *   <li>Scopes/claims are crafted by the test, so authorization edge cases
 *       (missing scope, wrong issuer, expired token, wrong signing key) are
 *       trivial to reproduce.</li>
 * </ul>
 *
 * Trade-off: you are no longer exercising the real IdP, so drift in token shape
 * (new claims, algorithm changes, audience enforcement) will not surface here.
 * Keep at least one {@code AbstractOAuth2IntegrationTest}-based test around for
 * that.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@DisplayName("BookController IT — Keycloak replaced by WireMock JWKS stub")
class BookControllerOAuth2StubIT {

  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static OAuth2Stubs oauth2Stubs;

  static {
    POSTGRES.start();
  }

  @BeforeAll
  static void startStubs() {
    WIREMOCK.start();
    oauth2Stubs = new OAuth2Stubs(WIREMOCK, "workshop");
    oauth2Stubs.stubOpenIdConfiguration();
  }

  @AfterAll
  static void stopStubs() {
    WIREMOCK.stop();
  }

  @DynamicPropertySource
  static void resourceServerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", oauth2Stubs::issuerUri);
  }

  @Autowired
  RestTestClient restTestClient;

  @Test
  @DisplayName("should return 2xx/404 when caller presents a token with books:read scope")
  void shouldReturnOkWhenTokenHasReadScope() {
    String token = oauth2Stubs.signedJwt("alice", "books:read");

    restTestClient.get()
      .uri("/api/books/1")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .exchange()
      .expectStatus().value(status -> {
        if (status != 200 && status != 404) {
          throw new AssertionError("Expected 200 or 404 but got " + status);
        }
      });
  }

  @Test
  @DisplayName("should return 403 when caller token is missing books:read scope")
  void shouldReturnForbiddenWhenTokenMissingReadScope() {
    String token = oauth2Stubs.signedJwt("alice", "profile");

    restTestClient.get()
      .uri("/api/books/1")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .exchange()
      .expectStatus().isForbidden();
  }

  @Test
  @DisplayName("should return 401 when no bearer token is provided")
  void shouldReturnUnauthorizedWhenNoToken() {
    restTestClient.get()
      .uri("/api/books/1")
      .exchange()
      .expectStatus().isUnauthorized();
  }
}
