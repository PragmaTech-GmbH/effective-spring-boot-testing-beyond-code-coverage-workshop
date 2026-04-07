package pragmatech.digital.workshops.lab3.experiment;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab3.event.BookCreatedEvent;
import pragmatech.digital.workshops.lab3.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class RecordApplicationEventsTest {

  @Autowired
  private ApplicationEvents events;

  @Autowired
  private BookService bookService;

  @Test
  void shouldPublishBookCreatedEventWhenCreatingBook() {
    BookCreationRequest request = new BookCreationRequest(
      "978-0134757599",
      "Effective Java",
      "Joshua Bloch",
      LocalDate.of(2018, 1, 6)
    );

    bookService.createBook(request);

    assertThat(events.stream(BookCreatedEvent.class)).hasSize(1);

    BookCreatedEvent publishedEvent = events.stream(BookCreatedEvent.class).findFirst().orElseThrow();
    assertThat(publishedEvent.isbn()).isEqualTo("978-0134757599");
    assertThat(publishedEvent.title()).isEqualTo("Effective Java");
    assertThat(publishedEvent.bookId()).isNotNull();
  }
}
