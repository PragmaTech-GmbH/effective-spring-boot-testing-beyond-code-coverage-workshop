package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pragmatech.digital.workshops.lab3.support.AbstractIntegrationTest;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "bookshelf.feature.killer-two=enabled",
    "bookshelf.feature.killer-two-timestamp=" + 1234567890L
  }
)
class ContextKiller2IT extends AbstractIntegrationTest {

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
