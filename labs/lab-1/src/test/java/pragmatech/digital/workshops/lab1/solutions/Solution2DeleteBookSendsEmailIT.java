package pragmatech.digital.workshops.lab1.solutions;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClient;
import pragmatech.digital.workshops.lab1.config.AbstractOAuth2IntegrationTest;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2 — End-to-end DELETE → email-on-delete check, three variants.
 *
 * <p>Each test arranges the "doomed" book a different way, then fires {@code DELETE /api/books/{id}}
 * with a bearer token for the {@code admin} user and polls Mailpit's REST API to verify the
 * notification email was actually rendered and delivered.
 *
 * <h3>Three ways to arrange the data</h3>
 * <ol>
 *   <li><b>{@code bookRepository.save(...)}</b> — simplest, works here because
 *       {@link AbstractOAuth2IntegrationTest} is <i>not</i> {@code @Transactional}, so the
 *       INSERT is committed before the HTTP call. <b>Careful:</b> if you (or a teammate) ever
 *       adds {@code @Transactional} to the test class, the save runs inside the test's
 *       transaction which is rolled back at the end. The controller thread that handles
 *       {@code DELETE /api/books/{id}} runs on Tomcat's request thread with its <i>own</i>
 *       {@link org.springframework.transaction.annotation.Transactional} scope — it will
 *       never see your yet-uncommitted row and you'll get a surprise 404. Different thread,
 *       different transaction, different visibility. This is one of the most common
 *       integration-test gotchas.</li>
 *   <li><b>{@code @Sql} script</b> — executes the INSERT through a plain JDBC connection in
 *       its own auto-committed transaction before the test body runs, so the row is
 *       guaranteed to be visible to the server thread regardless of any JPA / transaction
 *       config in the test. Bonus: you get to see the exact SQL in version control.</li>
 *   <li><b>Create through the real HTTP endpoint</b> — {@code POST /api/books} exercises the
 *       full stack (validation, {@code OpenLibrary} enrichment, security, JPA commit) and
 *       leaves the row committed by the time the response comes back, so it's also immune
 *       to the transaction-scope pitfall. Best for "full journey" demos, slowest to run.</li>
 * </ol>
 */
class Solution2DeleteBookSendsEmailIT extends AbstractOAuth2IntegrationTest {

  @Autowired
  BookRepository bookRepository;

  @Autowired
  RestTestClient restTestClient;

  @Test
    // @Transactional
  void shouldDeleteBookAndSendNotificationEmailWhenBookPreloadedViaRepository() {
    Book savedBook = bookRepository.save(new Book(
      "978-0000000001",
      "repository-preloaded",
      LocalDate.of(2024, 1, 1),
      "A Doomed Book",
      "Anonymous"));

    String token = KEYCLOAK.getAccessToken("admin", "admin");

    this.restTestClient.delete()
      .uri("/api/books/{id}", savedBook.getId())
      .header("Authorization", "Bearer " + token)
      .exchange()
      .expectStatus().isNoContent();

    assertThat(bookRepository.findById(savedBook.getId())).isEmpty();

    assertDeletionEmailReceived("A Doomed Book", "978-0000000001");
  }

  @Test
  @Sql("/sql/preload-doomed-book.sql")
  void shouldDeleteBookAndSendNotificationEmailWhenBookPreloadedViaSqlScript() {
    String token = KEYCLOAK.getAccessToken("admin", "admin");

    this.restTestClient.delete()
      .uri("/api/books/{id}", 9001L)
      .header("Authorization", "Bearer " + token)
      .exchange()
      .expectStatus().isNoContent();

    assertThat(bookRepository.findById(9001L)).isEmpty();

    assertDeletionEmailReceived("Preloaded via @Sql", "978-0000000042");
  }

  @Test
  void shouldDeleteBookAndSendNotificationEmailWhenBookCreatedViaHttpEndpoint() {
    String adminToken = KEYCLOAK.getAccessToken("admin", "admin");

    String createBookJson = """
      {
        "isbn": "978-0000000077",
        "internalName": "http-created",
        "availabilityDate": "2024-01-01"
      }
      """;

    String location = this.restTestClient.post()
      .uri("/api/books")
      .header("Authorization", "Bearer " + adminToken)
      .contentType(MediaType.APPLICATION_JSON)
      .body(createBookJson)
      .exchange()
      .expectStatus().isCreated()
      .returnResult(Void.class)
      .getResponseHeaders()
      .getFirst("Location");

    assertThat(location).as("POST /api/books must return a Location header").isNotNull();
    Long newBookId = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

    this.restTestClient.delete()
      .uri("/api/books/{id}", newBookId)
      .header("Authorization", "Bearer " + adminToken)
      .exchange()
      .expectStatus().isNoContent();

    assertThat(bookRepository.findById(newBookId)).isEmpty();

    assertDeletionEmailReceived(null, "978-0000000077");
  }

  private void assertDeletionEmailReceived(String expectedTitleOrNull, String expectedIsbn) {
    RestClient mailpitClient = RestClient.create(MAILPIT.getApiBaseUrl());

    Awaitility.await()
      .atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(200))
      .untilAsserted(() -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> inbox = mailpitClient.get()
          .uri("/api/v1/search?query=" + expectedIsbn)
          .retrieve()
          .body(Map.class);

        assertThat(inbox).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) inbox.get("messages");
        assertThat(messages)
          .as("expected deletion email for ISBN %s in Mailpit", expectedIsbn)
          .isNotEmpty();

        Map<String, Object> firstMessage = messages.get(0);
        String messageId = (String) firstMessage.get("ID");

        assertThat((String) firstMessage.get("Subject"))
          .startsWith("Book removed from library");

        @SuppressWarnings("unchecked")
        Map<String, Object> fullMessage = mailpitClient.get()
          .uri("/api/v1/message/" + messageId)
          .retrieve()
          .body(Map.class);

        assertThat(fullMessage).isNotNull();
        String htmlBody = (String) fullMessage.get("HTML");
        assertThat(htmlBody)
          .as("rendered FreeMarker template should include the deleted book's ISBN")
          .contains(expectedIsbn);
        if (expectedTitleOrNull != null) {
          assertThat(htmlBody)
            .as("rendered template should include the deleted book's title")
            .contains(expectedTitleOrNull);
        }
      });
  }
}
