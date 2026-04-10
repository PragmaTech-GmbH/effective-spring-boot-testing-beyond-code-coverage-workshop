package pragmatech.digital.workshops.lab2.solutions;

import java.time.LocalDate;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab2.config.AbstractOAuth2IntegrationTest;
import pragmatech.digital.workshops.lab2.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab2.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 1 — Full integration test for {@code POST /api/books} that stubs
 * OpenLibrary with WireMock and authenticates via a real Keycloak access token.
 */
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Disabled("Legacy pre-rewrite solution — superseded by SolutionCreateBookWireMockIT")
class Solution1WireMockCreateBookIT extends AbstractOAuth2IntegrationTest {

  private static final String ISBN = "978-0132350884";
  private static final String STUB_PATH_ISBN = "9780132350884";

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  WireMockServer wireMockServer;

  @Autowired
  BookRepository bookRepository;

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() {
    new OpenLibraryApiStub(wireMockServer).stubForSuccessfulBookResponse(STUB_PATH_ISBN);

    String token = fetchPasswordGrantToken("bob", "bob");

    String body = """
      {
        "isbn": "%s",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "%s"
      }
      """.formatted(ISBN, LocalDate.now().minusYears(2));

    ResponseEntity<Void> response = restTemplate.exchange(
      "/api/books",
      HttpMethod.POST,
      new HttpEntity<>(body, bearerHeaders(token)),
      Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    assertThat(bookRepository.findByIsbn(ISBN))
      .hasValueSatisfying(book -> assertThat(book.getThumbnailUrl()).isNotBlank());

    wireMockServer.verify(getRequestedFor(urlPathEqualTo("/isbn/" + STUB_PATH_ISBN)));
  }
}
