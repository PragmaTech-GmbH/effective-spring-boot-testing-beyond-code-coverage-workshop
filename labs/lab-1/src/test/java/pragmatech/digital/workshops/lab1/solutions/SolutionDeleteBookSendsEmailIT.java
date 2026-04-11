package pragmatech.digital.workshops.lab1.solutions;

import java.time.Duration;
import java.time.LocalDate;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.experiment.KeycloakContainer;
import pragmatech.digital.workshops.lab1.experiment.MailpitContainer;
import pragmatech.digital.workshops.lab1.repository.BookRepository;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution — End-to-end DELETE → email-on-delete check, three variants.
 *
 * <p>Each test arranges the "doomed" book a different way, then fires {@code DELETE /api/books/{id}}
 * with a bearer token for the {@code admin} user and polls Mailpit's REST API to verify the
 * notification email was actually rendered and delivered.
 *
 */
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SolutionDeleteBookSendsEmailIT {


  @ServiceConnection
  protected static final PostgreSQLContainer POSTGRES =
    new PostgreSQLContainer("postgres:16-alpine");

  protected static final KeycloakContainer KEYCLOAK = new KeycloakContainer();

  protected static final MailpitContainer MAILPIT = new MailpitContainer();

  static {
    POSTGRES.start();
    KEYCLOAK.start();
    MAILPIT.start();
  }

  @DynamicPropertySource
  static void resourceServerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KEYCLOAK::getIssuerUri);
    registry.add("spring.mail.host", MAILPIT::getHost);
    registry.add("spring.mail.port", MAILPIT::getSmtpPort);
  }

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
        ObjectNode inbox = mailpitClient.get()
          .uri("/api/v1/search?query=" + expectedIsbn)
          .retrieve()
          .body(ObjectNode.class);

        assertThat(inbox).isNotNull();
        assertThat(inbox.path("messages"))
          .as("expected deletion email for ISBN %s in Mailpit", expectedIsbn)
          .isNotEmpty();

        var firstMessage = inbox.path("messages").get(0);
        String messageId = firstMessage.path("ID").asText();

        assertThat(firstMessage.path("Subject").asText())
          .startsWith("Book removed from library");

        ObjectNode fullMessage = mailpitClient.get()
          .uri("/api/v1/message/" + messageId)
          .retrieve()
          .body(ObjectNode.class);

        assertThat(fullMessage).isNotNull();
        String htmlBody = fullMessage.path("HTML").asText();
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
