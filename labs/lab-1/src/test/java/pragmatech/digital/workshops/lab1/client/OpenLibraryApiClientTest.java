package pragmatech.digital.workshops.lab1.client;

import java.util.Optional;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pragmatech.digital.workshops.lab1.client.OpenLibraryApiClient.BookMetadata;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpenLibraryApiClient")
class OpenLibraryApiClientTest {

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(options().dynamicPort())
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    WebClient webClient = WebClient.builder().baseUrl(wireMock.baseUrl()).build();
    cut = new OpenLibraryApiClient(webClient);
  }

  @Nested
  @DisplayName("fetchMetadataForIsbn")
  class FetchMetadataForIsbn {

    @Test
    @DisplayName("should return title, author and thumbnail when OpenLibrary has a full record")
    void shouldReturnFullMetadataWhenOpenLibraryHasRecord() {
      String isbn = "9780321160768";
      wireMock.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("openlibrary/headfirst-java.json")));

      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

      assertThat(result).isPresent();
      assertThat(result.get().title()).isEqualTo("Head first Java");
      assertThat(result.get().author()).isEqualTo("Kathy Sierra");
      assertThat(result.get().thumbnailUrl()).isEqualTo("https://covers.openlibrary.org/b/id/388761-S.jpg");
    }

    @Test
    @DisplayName("should return empty when OpenLibrary has no record for the ISBN")
    void shouldReturnEmptyWhenOpenLibraryHasNoRecord() {
      String isbn = "9999999999999";
      wireMock.stubFor(get(urlPathEqualTo("/api/books"))
        .withQueryParam("bibkeys", equalTo(isbn))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("{}")));

      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return metadata without thumbnail when cover is missing")
    void shouldReturnMetadataWithoutThumbnailWhenCoverIsMissing() {
      String isbn = "9781234567890";
      String body = """
        {
          "9781234567890": {
            "title": "Untitled Work",
            "authors": [{ "name": "Jane Doe" }]
          }
        }
        """;
      wireMock.stubFor(get(urlPathEqualTo("/api/books"))
        .withQueryParam("bibkeys", equalTo(isbn))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(body)));

      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

      assertThat(result).isPresent();
      assertThat(result.get().title()).isEqualTo("Untitled Work");
      assertThat(result.get().author()).isEqualTo("Jane Doe");
      assertThat(result.get().thumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("should retry twice and then fail when OpenLibrary returns 500")
    void shouldRetryTwiceAndThenFailWhenOpenLibraryReturns500() {
      String isbn = "9780321160768";
      wireMock.stubFor(get(urlPathEqualTo("/api/books"))
        .withQueryParam("bibkeys", equalTo(isbn))
        .willReturn(aResponse().withStatus(500)));

      assertThatThrownBy(() -> cut.fetchMetadataForIsbn(isbn))
        .hasRootCauseInstanceOf(org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError.class);

      wireMock.verify(3, getRequestedFor(urlPathEqualTo("/api/books")));
    }
  }
}
