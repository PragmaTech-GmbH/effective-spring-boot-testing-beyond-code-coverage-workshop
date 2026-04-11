package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import pragmatech.digital.workshops.lab3.support.AbstractIntegrationTest;

@TestPropertySource(properties = {
  "bookshelf.feature.killer-three=on",
  "bookshelf.feature.killer-three-url=http://localhost:9999/unique"
})
class ContextKiller3IT extends AbstractIntegrationTest {

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
