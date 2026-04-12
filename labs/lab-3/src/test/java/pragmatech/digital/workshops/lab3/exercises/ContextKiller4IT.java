package pragmatech.digital.workshops.lab3.exercises;

import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab3.service.BookService;
import pragmatech.digital.workshops.lab3.support.OAuth2Stubs;

import static org.mockito.Mockito.when;

/**
 * Exercise — This integration test prevents Spring context caching.
 *
 * <h2>Your task</h2>
 * <ol>
 *   <li>Find the annotation or configuration that forces Spring to create a <b>new</b>
 *       ApplicationContext instead of reusing the cached one</li>
 *   <li>Remove or refactor it so this class shares the same context as the other ContextKiller ITs</li>
 *   <li>After fixing all five, extract the shared boilerplate into an {@code AbstractIntegrationTest}
 *       base class (see {@code solutions/AbstractIntegrationTest} for the target state)</li>
 * </ol>
 *
 * <h2>Hints</h2>
 * <ul>
 *   <li>Run {@code ./mvnw verify} and watch the logs — each "Started ContextKillerNIT" line
 *       means a fresh context was booted</li>
 *   <li>Compare this class line-by-line with the other four — what differs?</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ContextKiller4IT {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES =
    new PostgreSQLContainer("postgres:16-alpine");

  static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static final OAuth2Stubs OAUTH2_STUBS;

  static {
    POSTGRES.start();
    WIREMOCK.start();
    OAUTH2_STUBS = new OAuth2Stubs(WIREMOCK, "workshop");
    OAUTH2_STUBS.stubOpenIdConfiguration();
  }

  @DynamicPropertySource
  static void sharedProperties(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", OAUTH2_STUBS::issuerUri);
  }

  @Autowired
  RestTestClient restTestClient;

  @MockitoBean
  BookService bookService;

  @Test
  void shouldReturnMockedBooksList() {
    when(bookService.getAllBooks()).thenReturn(List.of());

    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }

  @Test
  void shouldReturnMockedBooksListAgain() {
    when(bookService.getAllBooks()).thenReturn(List.of());

    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }
}
