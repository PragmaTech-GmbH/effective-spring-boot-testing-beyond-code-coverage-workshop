package pragmatech.digital.workshops.lab2.solutions;

import java.time.LocalDate;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab2.config.MailpitContainer;
import pragmatech.digital.workshops.lab2.experiment.OAuth2Stubs;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution — Integration test for {@code POST /api/books} with a WireMock-stubbed
 * OpenLibrary API, exercised over a <b>real HTTP stack</b>.
 *
 * <p>This is the RANDOM_PORT flavour of the same scenario tested by
 * {@code BookControllerMockMvcIT}. Use it to compare the two approaches:
 *
 * <ul>
 *   <li><b>Real Tomcat</b> — {@code webEnvironment = RANDOM_PORT} starts the
 *       embedded servlet container on a random port. Every request goes over a
 *       real TCP socket, through the real Tomcat connector, the real Spring
 *       Security filter chain, and a real {@link org.springframework.security.oauth2.jwt.NimbusJwtDecoder}
 *       that actually fetches JWKS from WireMock on first use.</li>
 *
 *   <li><b>No {@code @Transactional} rollback</b> — the server runs on its own
 *       thread with its own transaction. The test thread can't roll it back,
 *       so we clean up explicitly via {@link #cleanup()} after every test.
 *       Forgetting this would leak rows between tests. This is the big
 *       operational difference from the MockMvc mode.</li>
 *
 *   <li><b>Real signed JWT, no Keycloak</b> — {@link OAuth2Stubs} piggybacks on
 *       the same WireMock instance to serve the OIDC discovery document and the
 *       JWKS endpoint. Tests mint JWTs with {@code oauth2Stubs.signedJwt(...)}
 *       and pass them in the {@code Authorization} header exactly like a real
 *       client would. The JWKS response is cached by NimbusJwtDecoder after the
 *       first token, so subsequent calls stay offline.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class SolutionCreateBookWireMockIT {

  private static final String ISBN = "978-0132350884";

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

  static final MailpitContainer MAILPIT = new MailpitContainer();

  static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static OAuth2Stubs oauth2Stubs;

  static {
    POSTGRES.start();
    MAILPIT.start();
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
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
      () -> oauth2Stubs.issuerUri());
  }

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  BookRepository bookRepository;

  /**
   * RANDOM_PORT commits are real: the embedded Tomcat runs the controller on
   * its own thread with its own transaction, so nothing the test's
   * {@code @Transactional} annotation does would roll those writes back.
   * Explicit cleanup keeps tests independent.
   */
  @AfterEach
  void cleanup() {
    bookRepository.deleteAll();
  }

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() {
    WIREMOCK.stubFor(get(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(ISBN))
      .withQueryParam("jscmd", equalTo("data"))
      .withQueryParam("format", equalTo("json"))
      .willReturn(ok()
        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .withBody("""
          {
            "978-0132350884": {
              "title": "Clean Code",
              "authors": [ { "name": "Robert C. Martin" } ],
              "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
            }
          }
          """)));

    String body = """
      {
        "isbn": "%s",
        "internalName": "clean-code-shelf-a3",
        "availabilityDate": "%s"
      }
      """.formatted(ISBN, LocalDate.now().plusDays(7));

    // Mint a real, RS256-signed JWT whose public key half is published through
    // our stubbed JWKS endpoint. NimbusJwtDecoder will fetch the JWKS once and
    // cache it for the rest of the run.
    String token = oauth2Stubs.signedJwt("alice", "books:write");

    restTestClient.post()
      .uri("/api/books")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .body(body)
      .exchange()
      .expectStatus().isCreated()
      .expectHeader().exists("Location");

    // The server has already committed by the time exchange() returns, so
    // this repository call is reading COMMITTED state from a different
    // transaction — a key contrast with the MockMvc/@Transactional flow.
    assertThat(bookRepository.findByIsbn(ISBN))
      .hasValueSatisfying(book -> {
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getThumbnailUrl())
          .isEqualTo("https://covers.openlibrary.org/b/id/8085499-S.jpg");
        assertThat(book.getInternalName()).isEqualTo("clean-code-shelf-a3");
      });

    WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(ISBN)));
  }
}
