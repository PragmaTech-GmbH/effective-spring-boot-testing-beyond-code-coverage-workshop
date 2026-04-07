package pragmatech.digital.workshops.lab3.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import pragmatech.digital.workshops.lab3.entity.BookStatus;

/**
 * DTO for book update requests using Java Record
 */
public record BookUpdateRequest(
  @NotBlank(message = "Title is required")
  String title,

  @NotBlank(message = "Author is required")
  String author,

  @NotNull(message = "Published date is required")
  @Past(message = "Published date must be in the past")
  LocalDate publishedDate,

  @NotNull(message = "Status is required")
  BookStatus status
) { }
