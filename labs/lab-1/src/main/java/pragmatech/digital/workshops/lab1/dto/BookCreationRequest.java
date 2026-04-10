package pragmatech.digital.workshops.lab1.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a new book. The library-internal fields are supplied by the user;
 * the remaining bibliographic data (title, author, thumbnail) is enriched from OpenLibrary.
 */
public record BookCreationRequest(
  @NotBlank(message = "ISBN is required")
  @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "ISBN must be in format 123-1234567890")
  String isbn,

  @NotBlank(message = "Internal name is required")
  String internalName,

  @NotNull(message = "Availability date is required")
  LocalDate availabilityDate
) { }
