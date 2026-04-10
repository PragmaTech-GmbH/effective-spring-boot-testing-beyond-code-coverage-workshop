package pragmatech.digital.workshops.lab4.client;

import java.time.Duration;
import java.util.Optional;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

/**
 * Client for interacting with the OpenLibrary Books API.
 *
 * <p>Uses the {@code /api/books?bibkeys=…&jscmd=data&format=json} endpoint, which returns
 * a JSON object keyed by ISBN with the bibliographic record we need to enrich books
 * during creation.
 */
//@Component
public class OpenLibraryApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(OpenLibraryApiClient.class);

  private final WebClient openLibraryWebClient;

  public OpenLibraryApiClient(WebClient openLibraryWebClient) {
    this.openLibraryWebClient = openLibraryWebClient;
  }

  /**
   * Fetch title, author and thumbnail for the given ISBN.
   *
   * @param isbn the ISBN to look up (OpenLibrary accepts both with and without hyphens)
   * @return the metadata, or {@link Optional#empty()} if OpenLibrary has no record for the ISBN
   */
  public Optional<BookMetadata> fetchMetadataForIsbn(String isbn) {
    LOG.info("Fetching OpenLibrary metadata for ISBN {}", isbn);

    JsonNode root = openLibraryWebClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/api/books")
        .queryParam("jscmd", "data")
        .queryParam("format", "json")
        .queryParam("bibkeys", isbn)
        .build())
      .retrieve()
      .bodyToMono(JsonNode.class)
      .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
      .block();

    JsonNode entry = root == null ? null : root.get(isbn);
    if (entry == null || entry.isMissingNode() || entry.isNull()) {
      LOG.info("OpenLibrary returned no record for ISBN {}", isbn);
      return Optional.empty();
    }

    BookMetadata metadata = new BookMetadata(
      text(entry, "title"),
      firstAuthorName(entry.get("authors")),
      text(entry.get("cover"), "small")
    );
    return Optional.of(metadata);
  }

  private static String text(JsonNode parent, String field) {
    if (parent == null) {
      return null;
    }
    JsonNode node = parent.get(field);
    if (node == null || node.isNull() || node.isMissingNode()) {
      return null;
    }
    String value = node.asText(null);
    return (value == null || value.isBlank()) ? null : value;
  }

  private static String firstAuthorName(JsonNode authorsArray) {
    if (authorsArray == null || !authorsArray.isArray() || authorsArray.isEmpty()) {
      return null;
    }
    return text(authorsArray.get(0), "name");
  }

  /**
   * Subset of OpenLibrary book metadata we enrich new books with.
   */
  public record BookMetadata(String title, String author, String thumbnailUrl) { }
}
