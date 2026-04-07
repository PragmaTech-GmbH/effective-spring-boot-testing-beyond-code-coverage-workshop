package pragmatech.digital.workshops.lab3.exception;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler for validation exceptions.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

  /**
   * Handle validation exceptions and return detailed problem information
   *
   * @param ex the validation exception
   * @return a problem detail with validation errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
      HttpStatus.BAD_REQUEST,
      "Validation failed for the request"
    );

    problemDetail.setTitle("Validation Error");
    problemDetail.setType(URI.create("https://api.bookshelf.com/errors/validation"));
    problemDetail.setProperty("timestamp", Instant.now());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    problemDetail.setProperty("errors", errors);

    return problemDetail;
  }
}
