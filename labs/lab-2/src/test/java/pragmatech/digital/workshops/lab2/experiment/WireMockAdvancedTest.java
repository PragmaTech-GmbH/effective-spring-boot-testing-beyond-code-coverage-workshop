package pragmatech.digital.workshops.lab2.experiment;

import java.util.Optional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient.BookMetadata;
import reactor.core.Exceptions;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Advanced WireMock features, demoed against the {@link OpenLibraryApiClient}.
 *
 * <p>Each {@code @Nested} class zooms in on one feature so you can walk through
 * them one at a time during the workshop:
 * <ol>
 *   <li>Response templating — dynamic bodies driven by request data.</li>
 *   <li>Proxying &amp; recording — capture real traffic once, replay forever.</li>
 *   <li>Stateful scenarios — a single URL returns different responses based on
 *       a named state machine (retries, eventual consistency, ...).</li>
 *   <li>Request verification — assert on the requests WireMock observed.</li>
 *   <li>Slow responses — simulate latency with {@code withFixedDelay(...)}.</li>
 * </ol>
 *
 * The {@link ProxyingAndRecording} test hits the real OpenLibrary API and is
 * {@link Disabled} by default; un-disable it locally when you want to refresh
 * the recorded stubs.
 */
class WireMockAdvancedTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort().notifier(new ConsoleNotifier(true)))  // true = verbose
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    cut = new OpenLibraryApiClient(
      WebClient.builder().baseUrl(wireMockServer.baseUrl()).build());

    wireMockServer.resetAll();
  }

  /**
   * Feature: Response Templating
   *
   * <p>WireMock can use Handlebars templates in response bodies to dynamically inject
   * values from the incoming request (headers, query params, body, URL segments).
   * Enable per-stub with {@code withTransformers("response-template")}.
   *
   * <p>Common helpers: {@code {{request.query.name.[0]}}}, {@code {{request.headers.X-Foo}}},
   * {@code {{jsonPath request.body '$.id'}}}, {@code {{now format='yyyy-MM-dd'}}}.
   */
  @Nested
  class ResponseTemplating {

    @Test
    void shouldInjectBibkeyQueryParamIntoResponseBody() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
            {
              "{{request.query.bibkeys.[0]}}": {
                "title": "Resolved for {{request.query.bibkeys.[0]}}",
                "authors": [ { "name": "Template Author" } ],
                "cover": { "small": "https://example.com/cover.jpg" }
              }
            }
            """)
          .withTransformers("response-template")));

      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

      assertThat(result).isPresent();
      assertThat(result.get().title()).isEqualTo("Resolved for 9780132350884");
      assertThat(result.get().author()).isEqualTo("Template Author");
    }
  }

  /**
   * Feature: Proxying &amp; Recording
   *
   * <p>WireMock can act as a recording proxy: forward requests to a real upstream,
   * capture the responses as stub mappings, then replay them offline in future runs.
   *
   * <p>Typical workflow (run once against production/staging):
   * <pre>
   *   recordingProxy.startRecording(recordSpec().forTarget("https://openlibrary.org").build());
   *   // ... exercise the code under test (real HTTP traffic flows through)
   *   recordingProxy.stopRecording();  // stubs saved to mappings/ + __files/
   * </pre>
   * Subsequent CI runs skip the real network entirely and replay from disk.
   *
   * <p>This test hits the real OpenLibrary API on the first call so WireMock can capture
   * an authentic response, then replays it offline on every subsequent call.
   */
  @Nested
  @Disabled
  class ProxyingAndRecording {

    private WireMockServer recordingProxy;

    @BeforeEach
    void setUpRecordingProxy() {
      recordingProxy = new WireMockServer(wireMockConfig().dynamicPort());
      recordingProxy.start();
    }

    @AfterEach
    void tearDownRecordingProxy() {
      recordingProxy.stop();
    }

    @Test
    void shouldRecordLiveResponseAndReplayOfflineWithoutUpstream() {
      String isbn = "9780132350884";

      recordingProxy.startRecording(
        recordSpec().forTarget("https://openlibrary.org").makeStubsPersistent(true).build());

      OpenLibraryApiClient proxyClient = new OpenLibraryApiClient(
        WebClient.builder().baseUrl(recordingProxy.baseUrl()).build());

      Optional<BookMetadata> liveResponse = proxyClient.fetchMetadataForIsbn(isbn);
      assertThat(liveResponse).isPresent();
      assertThat(liveResponse.get().title()).isEqualTo("Clean Code");

      recordingProxy.stopRecording();

      Optional<BookMetadata> replayedResponse = proxyClient.fetchMetadataForIsbn(isbn);

      assertThat(replayedResponse).isPresent();
      assertThat(replayedResponse.get().title()).isEqualTo("Clean Code");
    }
  }

  /**
   * Feature: Stateful Scenarios
   *
   * <p>WireMock scenarios let a single stub URL return different responses based on
   * a named state machine. Each stub declares which state it matches and what state
   * to transition to after responding.
   *
   * <p>Useful for simulating: retry / eventual consistency, pagination, CQRS
   * read-after-write lag, resource lifecycle (PENDING → ACTIVE → DELETED), etc.
   *
   * <p>Note: the lab-2 {@link OpenLibraryApiClient} already retries twice on failure,
   * so a single {@code serverError()} followed by a success will be handled
   * transparently inside one call.
   */
  @Nested
  class StatefulScenarios {

    @Test
    void shouldRecoverAutomaticallyWhenFirstCallFailsAndRetrySucceeds() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .inScenario("eventual-consistency")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(serverError())
        .willSetStateTo("recovered"));

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .inScenario("eventual-consistency")
        .whenScenarioStateIs("recovered")
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
            {
              "9780132350884": {
                "title": "Clean Code",
                "authors": [ { "name": "Robert C. Martin" } ],
                "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
              }
            }
            """)));

      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);

      assertThat(result).isPresent();
      assertThat(result.get().title()).isEqualTo("Clean Code");

      wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/api/books")));
    }

    @Test
    void shouldFailWhenUpstreamStaysDownLongerThanRetriesAllow() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(serverError()));

      assertThatThrownBy(() -> cut.fetchMetadataForIsbn(isbn));
    }
  }

  /**
   * Feature: Request Verification
   *
   * <p>WireMock records every incoming request. Use {@code verify(...)} to assert on
   * how many matching requests were received, or on the exact request shape
   * (headers, body, query params). Handy for proving your client doesn't over- or
   * under-call the upstream.
   */
  @Nested
  class RequestVerification {

    @Test
    void shouldVerifyExactNumberOfRequestsMade() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
            {
              "9780132350884": {
                "title": "Clean Code",
                "authors": [ { "name": "Robert C. Martin" } ],
                "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
              }
            }
            """)));

      cut.fetchMetadataForIsbn(isbn);
      cut.fetchMetadataForIsbn(isbn);

      // Simple count verification: "I expect exactly 2 GETs to /api/books"
      wireMockServer.verify(2, getRequestedFor(urlPathMatching("/api/books")));
    }

    /**
     * Companion example: verifying a request <em>body</em>.
     *
     * <p>The {@link OpenLibraryApiClient} only issues GET requests, so we
     * exercise a raw {@link WebClient} against a dedicated POST stub to show
     * how {@code withRequestBody(equalToJson(...))} works. The same pattern
     * applies to any client that sends POST/PUT/PATCH payloads.
     */
    @Test
    void shouldVerifyRequestBodyOnPostRequest() {
      wireMockServer.stubFor(post(urlPathEqualTo("/api/books"))
        .willReturn(created()));

      WebClient webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();

      String createBookJson = """
        {
          "isbn": "9780132350884",
          "title": "Clean Code",
          "author": "Robert C. Martin"
        }
        """;

      webClient.post()
        .uri("/api/books")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(createBookJson)
        .retrieve()
        .toBodilessEntity()
        .block();

      wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/api/books"))
        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
        // equalToJson does a structural comparison — key order and
        // whitespace are ignored, so the assertion is not fragile.
        .withRequestBody(equalToJson("""
          {
            "isbn": "9780132350884",
            "title": "Clean Code",
            "author": "Robert C. Martin"
          }
          """))
        // Alternative body matchers you can use instead:
        //   .withRequestBody(containing("Clean Code"))
        //   .withRequestBody(matchingJsonPath("$.isbn", equalTo("9780132350884")))
        //   .withRequestBody(matching(".*Clean Code.*"))
      );
    }
  }

  /**
   * Feature: Slow Responses
   *
   * <p>{@code withFixedDelay(ms)} makes WireMock hold the response for a fixed
   * amount of time before flushing it — perfect for asserting timeout and
   * circuit-breaker behaviour without actually depending on network conditions.
   */
  @Nested
  class SlowResponses {

    @Test
    void shouldStillRespondWhenResponseIsDelayed() {
      String isbn = "9780132350884";
      int delayMs = 500;

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
            {
              "9780132350884": {
                "title": "Clean Code",
                "authors": [ { "name": "Robert C. Martin" } ],
                "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
              }
            }
            """)
          .withFixedDelay(delayMs)));

      long start = System.currentTimeMillis();
      Optional<BookMetadata> result = cut.fetchMetadataForIsbn(isbn);
      long elapsed = System.currentTimeMillis() - start;

      assertThat(result).isPresent();
      assertThat(result.get().title()).isEqualTo("Clean Code");
      assertThat(elapsed).isGreaterThanOrEqualTo(delayMs);
    }
  }
}
