package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import pragmatech.digital.workshops.lab3.support.AbstractIntegrationTest;

@ActiveProfiles("killer-five")
class ContextKiller5IT extends AbstractIntegrationTest {

  @Test
  void shouldReturnOkOnGetAllBooks() {
    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }

  @Test
  void shouldReturnOkOnGetAllBooksAgain() {
    restTestClient.get()
      .uri("/api/books")
      .exchange()
      .expectStatus().isOk();
  }
}
