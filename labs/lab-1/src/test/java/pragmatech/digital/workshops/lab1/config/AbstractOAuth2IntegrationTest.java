package pragmatech.digital.workshops.lab1.config;

import java.util.Map;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab1.experiment.KeycloakContainer;
import pragmatech.digital.workshops.lab1.experiment.MailpitContainer;

/**
 * Base class for integration tests that need a real OAuth2 access token signed by
 * the Keycloak Testcontainers.
 */
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractOAuth2IntegrationTest {


}
