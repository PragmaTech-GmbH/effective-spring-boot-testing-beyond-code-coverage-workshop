package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;

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
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab2.config.MailpitContainer;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

/**
 * Experiment — a BookController integration test that skips Keycloak entirely.
 *
 * <p>Instead of booting a Keycloak Testcontainers (as {@code AbstractOAuth2IntegrationTest}
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
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerOAuth2StubIT {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

  static final MailpitContainer MAILPIT = new MailpitContainer();

  static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static OAuth2Stubs oauth2Stubs;

  static {
    POSTGRES.start();
  }

  @BeforeAll
  static void startStubs() {
    WIREMOCK.start();
    MAILPIT.start();
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
    registry.add("spring.mail.host", MAILPIT::getHost);
    registry.add("spring.mail.port", MAILPIT::getSmtpPort);
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
  }

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  BookRepository bookRepository;

  @Test
  void shouldReturnOkWhenTokenHasReadScope() {

    Book savedBook = this.bookRepository.save(new Book(
      "978-0000000001",
      "repository-preloaded",
      LocalDate.of(2024, 1, 1),
      "A Doomed Book",
      "Anonymous"));

    String token = oauth2Stubs.signedJwt("alice", "books:read");

    this.restTestClient.get()
      .uri("/api/books/" + savedBook.getId())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .exchange()
      .expectStatus()
      .is2xxSuccessful();
  }

  @Test
  @DisplayName("should return 403 when caller token is missing books:read scope")
  void shouldReturnForbiddenWhenTokenMissingReadScope() {
    String token = oauth2Stubs.signedJwt("alice", "profile");

    restTestClient.get()
      .uri("/api/books/1")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .exchange()
      .expectStatus()
      .isForbidden();
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
