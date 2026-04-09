package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerIT {

  private static final Logger LOG = LoggerFactory.getLogger(BookControllerIT.class);

  @Autowired
  private RestTestClient restTestClient;

  @ServiceConnection
  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine").withReuse(true);
  private static final KeycloakContainer KEYCLOAK = new KeycloakContainer().withReuse(true);
  private static final MailpitContainer MAILPIT = new MailpitContainer().withReuse(true);

  static {
    KEYCLOAK.start();
    MAILPIT.start();
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KEYCLOAK::getIssuerUri);
    registry.add("spring.mail.host", MAILPIT::getHost);
    registry.add("spring.mail.port", MAILPIT::getSmtpPort);

    LOG.info("Mailpit UI is accessible at: http://localhost:{}", MAILPIT.getHttpPort());
  }

  @Test
  void givenValidJWTThenShouldBeAbleToCreateBook() {
    String jwtToken = KEYCLOAK.getAccessToken("admin", "admin");

    this.restTestClient
      .post()
      .uri("/api/books")
      .headers(headers -> headers.setBearerAuth(jwtToken))
      .contentType(MediaType.APPLICATION_JSON)
      .body("""
        {
          "isbn":"978-0201616224",
          "internalName":"SHELF_1",
          "availabilityDate":"2027-01-01"
        }
        """)
      .exchangeSuccessfully();
  }

}
