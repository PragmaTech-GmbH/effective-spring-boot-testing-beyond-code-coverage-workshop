package pragmatech.digital.workshops.lab1.solutions;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pragmatech.digital.workshops.lab1.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab1.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab1.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab1.event.BookCreatedEvent;
import pragmatech.digital.workshops.lab1.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Solution2RecordApplicationEventsTest {

  @Autowired
  private ApplicationEvents events;

  @Autowired
  private BookService bookService;

  @Test
  void shouldPublishBookCreatedEventWhenCreatingBook() {
    BookCreationRequest request = new BookCreationRequest(
      "978-0201633610",
      "The Pragmatic Programmer",
      "Andrew Hunt",
      LocalDate.of(1999, 10, 20)
    );

    bookService.createBook(request);

    assertThat(events.stream(BookCreatedEvent.class)).hasSize(1);

    BookCreatedEvent publishedEvent = events.stream(BookCreatedEvent.class).findFirst().orElseThrow();
    assertThat(publishedEvent.isbn()).isEqualTo("978-0201633610");
    assertThat(publishedEvent.title()).isEqualTo("The Pragmatic Programmer");
    assertThat(publishedEvent.bookId()).isNotNull().isPositive();
  }
}
