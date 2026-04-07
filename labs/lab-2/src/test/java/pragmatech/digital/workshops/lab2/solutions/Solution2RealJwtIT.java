package pragmatech.digital.workshops.lab2.solutions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pragmatech.digital.workshops.lab2.config.AbstractOAuth2IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2 — Real, signed JWT issued by the Keycloak Testcontainer.
 *
 * Two scenarios:
 * <ol>
 *   <li>{@code client_credentials} grant — service-to-service token, default scopes
 *       {@code books:read books:write}.</li>
 *   <li>Password grant for the seeded users {@code alice} (read only),
 *       {@code bob} (write only) and {@code admin} (both). The granted authorities
 *       come from the user's realm roles via the custom
 *       {@code JwtAuthenticationConverter} in {@code SecurityConfig}.</li>
 * </ol>
 */
class Solution2RealJwtIT extends AbstractOAuth2IntegrationTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void shouldReject401WithoutToken() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/books/1", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldReadBooksWithClientCredentialsToken() {
    String token = fetchClientCredentialsToken();

    ResponseEntity<String> response = restTemplate.exchange(
      "/api/books/1",
      HttpMethod.GET,
      new HttpEntity<>(bearerHeaders(token)),
      String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldAllowAliceToReadButRejectWriteAttempts() {
    String aliceToken = fetchPasswordGrantToken("alice", "alice");

    ResponseEntity<String> readResponse = restTemplate.exchange(
      "/api/books/1",
      HttpMethod.GET,
      new HttpEntity<>(bearerHeaders(aliceToken)),
      String.class);
    assertThat(readResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
      "/api/books/1",
      HttpMethod.DELETE,
      new HttpEntity<>(bearerHeaders(aliceToken)),
      Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
