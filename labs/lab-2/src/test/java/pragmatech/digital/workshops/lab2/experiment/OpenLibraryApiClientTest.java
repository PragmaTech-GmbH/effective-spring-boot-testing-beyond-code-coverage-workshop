package pragmatech.digital.workshops.lab2.experiment;

import java.util.Optional;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient.BookMetadata;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Introductory WireMock test for the {@link OpenLibraryApiClient}.
 *
 * <p>Demonstrates the minimum you need to stub an HTTP collaborator:
 * <ul>
 *   <li>{@link WireMockExtension} — JUnit 5 extension that boots a WireMock server
 *       on a random port for the test class and shuts it down afterwards.</li>
 *   <li>{@code stubFor(...)} — define what the fake upstream should return for a
 *       given request shape.</li>
 *   <li>{@code wireMockServer.baseUrl()} — feed into the {@link WebClient} so the
 *       client under test hits the stub instead of the real OpenLibrary API.</li>
 * </ul>
 *
 */
class OpenLibraryApiClientTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    cut = new OpenLibraryApiClient(
      WebClient.builder().baseUrl(wireMockServer.baseUrl()).build());
  }

  @Test
  void shouldReturnMetadataWhenApiReturnsValidResponse() {
    String isbn = "9780132350884";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
            {
              "9780132350884": {
                "title": "Clean Code",
                "authors": [
                  { "name": "Robert C. Martin" }
                ],
                "cover": {
                  "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg"
                }
              }
            }
            """)));

    Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

    assertThat(result).isPresent();
    assertThat(result.get().title()).isEqualTo("Clean Code");
    assertThat(result.get().author()).isEqualTo("Robert C. Martin");
    assertThat(result.get().thumbnailUrl())
      .isEqualTo("https://covers.openlibrary.org/b/id/8085499-S.jpg");
  }

  @Test
  void shouldReturnEmptyWhenApiReturnsEmptyObjectForIsbn() {
    String isbn = "9999999999";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("{}")));

    Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenServerReturnsInternalError() {
    String isbn = "9780132350884";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withStatus(500)
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("{\"error\": \"Internal Server Error\"}")));

    assertThatThrownBy(() -> cut.fetchMetadataForIsbn(isbn))
      .hasRootCauseInstanceOf(WebClientResponseException.InternalServerError.class);
  }
}
