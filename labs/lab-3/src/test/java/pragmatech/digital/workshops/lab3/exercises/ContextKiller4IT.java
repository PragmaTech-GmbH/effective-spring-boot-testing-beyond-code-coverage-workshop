package pragmatech.digital.workshops.lab3.exercises;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab3.service.BookService;
import pragmatech.digital.workshops.lab3.support.AbstractIntegrationTest;

import static org.mockito.Mockito.when;

class ContextKiller4IT extends AbstractIntegrationTest {

  @MockitoBean
  BookService bookService;

  @Test
  void shouldReturnMockedBooksList() {
    when(bookService.getAllBooks()).thenReturn(List.of());

    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }

  @Test
  void shouldReturnMockedBooksListAgain() {
    when(bookService.getAllBooks()).thenReturn(List.of());

    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }
}
