package pragmatech.digital.workshops.lab2.solutions;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pragmatech.digital.workshops.lab2.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 3 — A {@code RANDOM_PORT} test that cleans up via {@code @AfterEach}
 * because {@code @Transactional} rollback no longer applies once the request is
 * served on a different thread.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Disabled("Legacy pre-rewrite solution — needs JwtDecoder wiring; kept as reference only")
class Solution3MockVsRandomPortIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  BookRepository bookRepository;

  @AfterEach
  void cleanUp() {
    bookRepository.deleteAll();
  }

  @Test
  void shouldServeBooksOverRealHttp() {
    String uniqueIsbn = "9780000" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 6);
    bookRepository.save(new Book(uniqueIsbn, "random-port-shelf", LocalDate.now().minusYears(1), "Random Port", "Solution"));

    ResponseEntity<String> response = restTemplate.getForEntity("/api/books", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains(uniqueIsbn);
  }
}
