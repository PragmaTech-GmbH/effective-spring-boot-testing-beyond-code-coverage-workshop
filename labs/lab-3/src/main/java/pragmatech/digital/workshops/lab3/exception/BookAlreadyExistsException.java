package pragmatech.digital.workshops.lab3.exception;

/**
 * Exception thrown when attempting to create a book that already exists.
 */
public class BookAlreadyExistsException extends RuntimeException {

  public BookAlreadyExistsException(String isbn) {
    super("Book with ISBN " + isbn + " already exists");
  }
}
