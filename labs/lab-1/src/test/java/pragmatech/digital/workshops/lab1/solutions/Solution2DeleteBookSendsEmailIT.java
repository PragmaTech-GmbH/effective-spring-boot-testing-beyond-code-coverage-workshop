package pragmatech.digital.workshops.lab1.solutions;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import pragmatech.digital.workshops.lab1.config.AbstractOAuth2IntegrationTest;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2 — End-to-end DELETE → email-on-delete check using a real bearer token
 * fetched from the Keycloak Testcontainer.
 */
class Solution2DeleteBookSendsEmailIT extends AbstractOAuth2IntegrationTest {

  static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:v1.20")
    .withExposedPorts(1025, 8025)
    .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
    .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");

  static {
    MAILPIT.start();
  }

  @DynamicPropertySource
  static void mailProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mail.host", MAILPIT::getHost);
    registry.add("spring.mail.port", () -> MAILPIT.getMappedPort(1025));
  }

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  BookRepository bookRepository;

  @LocalServerPort
  int port;

  @Test
  void shouldSendDeletionEmailWhenBookIsDeleted() {
    Book savedBook = bookRepository.save(new Book(
      "9780000000001", "A Doomed Book", "Anonymous", LocalDate.of(2024, 1, 1)));

    String token = fetchPasswordGrantToken("admin", "admin");

    ResponseEntity<Void> response = restTemplate.exchange(
      "/api/books/" + savedBook.getId(),
      HttpMethod.DELETE,
      new HttpEntity<>(bearerHeaders(token)),
      Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    String mailpitBaseUrl = "http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(8025);
    RestClient mailpitClient = RestClient.create(mailpitBaseUrl);

    Awaitility.await()
      .atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(200))
      .untilAsserted(() -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = mailpitClient.get()
          .uri("/api/v1/messages")
          .retrieve()
          .body(Map.class);

        assertThat(body).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).isNotEmpty();
        assertThat((String) messages.get(0).get("Subject")).startsWith("Book removed from library");
      });
  }
}
