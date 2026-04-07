package pragmatech.digital.workshops.lab3.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle BookAlreadyExistsException by returning HTTP 400 Bad Request with RFC 7807 Problem Details.
   *
   * @param ex the exception
   * @return a ProblemDetail with status 400 and error information
   */
  @ExceptionHandler(BookAlreadyExistsException.class)
  public ProblemDetail handleBookAlreadyExistsException(BookAlreadyExistsException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
      HttpStatus.BAD_REQUEST,
      ex.getMessage()
    );

    problemDetail.setTitle("Book Already Exists");
    problemDetail.setType(URI.create("https://api.bookshelf.com/errors/book-already-exists"));
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }
}
