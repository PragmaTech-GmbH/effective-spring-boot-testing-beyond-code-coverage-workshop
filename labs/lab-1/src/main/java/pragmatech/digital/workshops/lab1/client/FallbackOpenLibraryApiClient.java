package pragmatech.digital.workshops.lab1.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//@Component
public class FallbackOpenLibraryApiClient extends OpenLibraryApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(FallbackOpenLibraryApiClient.class);

  private static final BookMetadata HARDCODED_METADATA = new BookMetadata(
    "Effective Spring Boot Testing (Offline Demo Edition)",
    "Philip Riecks",
    "https://covers.openlibrary.org/b/id/240727-S.jpg"
  );

  public FallbackOpenLibraryApiClient() {
    super(null);
  }

  @Override
  public Optional<BookMetadata> fetchMetadataForIsbn(String isbn) {
    LOG.warn("Using FALLBACK OpenLibrary client for ISBN {} — returning hardcoded metadata", isbn);
    return Optional.of(HARDCODED_METADATA);
  }
}
