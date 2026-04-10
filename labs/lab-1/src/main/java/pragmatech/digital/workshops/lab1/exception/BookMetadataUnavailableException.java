package pragmatech.digital.workshops.lab1.exception;

/**
 * Thrown when OpenLibrary has no usable metadata for the given ISBN and the book
 * therefore cannot be enriched during creation.
 */
public class BookMetadataUnavailableException extends RuntimeException {

  private final String isbn;

  public BookMetadataUnavailableException(String isbn) {
    super("Could not fetch OpenLibrary metadata for ISBN " + isbn);
    this.isbn = isbn;
  }

  public String getIsbn() {
    return isbn;
  }
}
