package pragmatech.digital.workshops.lab3.solutions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab3.support.OAuth2Stubs;

/**
 * Shared base class for every integration test in lab 3 — the "solution"
 * side of the context caching exercise.
 *
 * <p><strong>Why a single base class?</strong> Lab 3 zooms in on two levers
 * that let a Spring Boot test suite run fast:
 *
 * <ol>
 *   <li><strong>Context caching.</strong> If every test class reuses the exact
 *       same {@link SpringBootTest @SpringBootTest} configuration (annotations,
 *       dynamic properties, imports), Spring caches the {@code ApplicationContext}
 *       and boots it only once for the whole suite. Extending this class is the
 *       simplest way to guarantee that.</li>
 *   <li><strong>Parallel class execution.</strong> See
 *       {@code src/test/resources/junit-platform.properties} —
 *       {@code parallel.mode.classes.default = concurrent}. A shared, stateless
 *       context and a fast stub-based IdP are what make that safe.</li>
 * </ol>
 *
 * <p><strong>What this base wires up:</strong>
 * <ul>
 *   <li>A single Postgres container, started once as a {@code static} field and
 *       exposed via {@link ServiceConnection}. Flyway migrations run against
 *       it at context refresh time.</li>
 *   <li>A single {@link WireMockServer} that plays two roles:
 *       <ol>
 *         <li>the fake OpenLibrary upstream (hit via
 *             {@code book.metadata.api.url}),</li>
 *         <li>the fake OIDC issuer (hit via
 *             {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}),
 *             fronted by {@link OAuth2Stubs}.</li>
 *       </ol>
 *       One server, one port, one set of stubs — no Keycloak container.</li>
 *   <li>A {@link TestConfiguration} that replaces
 *       {@code FallbackOpenLibraryApiClient} with the real
 *       {@link OpenLibraryApiClient} (marked {@code @Primary}) so integration
 *       tests actually hit WireMock.</li>
 *   <li>An auto-configured {@link RestTestClient} for exercising endpoints.</li>
 * </ul>
 *
 * <p>Subclasses should <strong>not</strong> add class-level annotations that
 * mutate the context (e.g. {@code @TestPropertySource}, extra {@code @Import},
 * profile overrides) — every deviation forks the context cache and kills the
 * caching win. Compare with the {@code exercises/ContextKiller*IT} tests to
 * see exactly how much duplication and how many broken cache keys this one
 * base class eliminates.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(AbstractIntegrationTest.RealOpenLibraryClientConfig.class)
public abstract class AbstractIntegrationTest {

  @ServiceConnection
  protected static final PostgreSQLContainer POSTGRES =
    new PostgreSQLContainer("postgres:16-alpine");

  protected static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  protected static final OAuth2Stubs OAUTH2_STUBS;

  static {
    POSTGRES.start();
    WIREMOCK.start();
    OAUTH2_STUBS = new OAuth2Stubs(WIREMOCK, "workshop");
    OAUTH2_STUBS.stubOpenIdConfiguration();
  }

  @DynamicPropertySource
  static void sharedProperties(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", OAUTH2_STUBS::issuerUri);
  }

  @Autowired
  protected RestTestClient restTestClient;

  /**
   * Swaps the default {@code FallbackOpenLibraryApiClient} bean for a real
   * {@link OpenLibraryApiClient} pointed at the shared WireMock server, so
   * integration tests actually exercise the HTTP client.
   */
  @TestConfiguration
  public static class RealOpenLibraryClientConfig {

    @Bean
    @Primary
    OpenLibraryApiClient openLibraryApiClient(WebClient openLibraryWebClient) {
      return new OpenLibraryApiClient(openLibraryWebClient);
    }
  }
}
