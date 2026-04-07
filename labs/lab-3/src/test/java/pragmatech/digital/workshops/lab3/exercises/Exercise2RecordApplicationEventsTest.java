package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.event.BookCreatedEvent;
import pragmatech.digital.workshops.lab3.service.BookService;

/**
 * Exercise 2: Use @RecordApplicationEvents to verify that BookCreatedEvent is published.
 *
 * <p>Tasks:
 * <ol>
 *   <li>Create a {@link BookService#createBook} call with a valid {@code BookCreationRequest}
 *       (use ISBN "978-0201633610" which is pre-stubbed in WireMock).</li>
 *   <li>Assert that exactly one {@link BookCreatedEvent} was published.</li>
 *   <li>Assert the event fields: isbn, title, and bookId.</li>
 * </ol>
 *
 * <p>Hint: Inject {@link ApplicationEvents} as a field and use
 * {@code events.stream(BookCreatedEvent.class)} to access published events.
 *
 * <p>Run with: {@code ./mvnw test -Dtest=Exercise2RecordApplicationEventsTest}
 */
@SpringBootTest
@RecordApplicationEvents
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Exercise2RecordApplicationEventsTest {

  @Autowired
  private ApplicationEvents events;

  @Autowired
  private BookService bookService;

  @Test
  void shouldPublishBookCreatedEventWhenCreatingBook() {
    // TODO: Create a book using BookService and assert the BookCreatedEvent was published
    // Hint: Use BookCreationRequest with isbn "978-0201633610", a title, author, and past date
    // Hint: Use events.stream(BookCreatedEvent.class).count() to check event count
  }
}
