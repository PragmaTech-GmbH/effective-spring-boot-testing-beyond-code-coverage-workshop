package pragmatech.digital.workshops.lab3.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pragmatech.digital.workshops.lab3.dto.BookMetadataResponse;

/**
 * Client for interacting with the OpenLibrary API.
 */
@Component
public class OpenLibraryApiClient {

  private static final Logger logger = LoggerFactory.getLogger(OpenLibraryApiClient.class);

  private final WebClient webClient;

  @Autowired
  public OpenLibraryApiClient(WebClient openLibraryWebClient) {
    this.webClient = openLibraryWebClient;
  }

  public BookMetadataResponse getBookByIsbn(String isbn) {
    return webClient.get()
      .uri("/isbn/{isbn}", isbn)
      .retrieve()
      .bodyToMono(BookMetadataResponse.class)
      .block();
  }
}
