package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise — Integration test for {@code POST /api/books} with a WireMock-stubbed
 * OpenLibrary client.
 *
 * <p>Background:
 * <ul>
 *   <li>{@code BookController#createBook} accepts a {@link
 *       pragmatech.digital.workshops.lab2.dto.BookCreationRequest} containing
 *       {@code isbn}, {@code internalName} and {@code availabilityDate}.</li>
 *   <li>{@link pragmatech.digital.workshops.lab2.service.BookService#createBook}
 *       then calls {@link pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient}
 *       to enrich the persisted book with {@code title}, {@code author} and
 *       {@code thumbnailUrl} pulled from OpenLibrary.</li>
 *   <li>The endpoint is protected: callers need a JWT with the
 *       {@code SCOPE_books:write} authority (see {@code SecurityConfig}).</li>
 * </ul>
 *
 * <p>Your job is to wire a full-stack integration test that:
 * <ol>
 *   <li>Boots the Spring context with a PostgreSQL Testcontainer (use
 *       {@code @ServiceConnection}).</li>
 *   <li>Starts a WireMock server and stubs {@code GET /api/books} on it so it
 *       returns a realistic OpenLibrary response keyed by the raw ISBN, for
 *       example:
 *       <pre>
 *       {
 *         "9780132350884": {
 *           "title": "Clean Code",
 *           "authors": [ { "name": "Robert C. Martin" } ],
 *           "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
 *         }
 *       }
 *       </pre>
 *   </li>
 *   <li>Overrides {@code book.metadata.api.url} with the WireMock base URL via
 *       {@code @DynamicPropertySource} so the {@code openLibraryWebClient} bean
 *       points at the stub.</li>
 *   <li>Swaps the {@code FallbackOpenLibraryApiClient} bean (which returns
 *       hardcoded metadata) for the real
 *       {@link pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient} — a
 *       {@code @TestConfiguration} exposing it as {@code @Primary} is the
 *       easiest route.</li>
 *   <li>Issues {@code POST /api/books} with a valid body and an authenticated
 *       JWT. Two options for the token:
 *       <ul>
 *         <li>{@code MockMvc} +
 *             {@code SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write"))}
 *             — fastest.</li>
 *         <li>{@code TestRestTemplate} + a real token from Keycloak
 *             ({@code AbstractOAuth2IntegrationTest#fetchPasswordGrantToken}).</li>
 *       </ul>
 *   </li>
 *   <li>Asserts:
 *       <ul>
 *         <li>HTTP {@code 201 Created} and a {@code Location} header pointing at
 *             the new resource.</li>
 *         <li>The persisted {@code Book} carries the stubbed {@code title},
 *             {@code author} and {@code thumbnailUrl}.</li>
 *         <li>WireMock received exactly one {@code GET /api/books} with the
 *             expected {@code bibkeys} query parameter
 *             ({@code wireMockServer.verify(...)}).</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>Hints:
 * <ul>
 *   <li>The request ISBN format is {@code 123-1234567890} (hyphenated), but the
 *       {@code BookCreationRequest} is stored as-is and forwarded to the client.
 *       The query parameter WireMock receives is whatever {@code request.isbn()}
 *       is — keep the test consistent.</li>
 *   <li>{@code WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build()}
 *       is the JUnit 5-friendly way to manage the server lifecycle.</li>
 *   <li>See {@code experiment/OpenLibraryApiClientTest} for the JSON shape the
 *       client expects.</li>
 * </ul>
 */
@SpringBootTest
@Disabled("TODO: implement the POST /api/books integration test with a WireMock-stubbed OpenLibrary API")
class ExerciseCreateBookWireMockIT {

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() {
    // TODO: follow the steps in the class Javadoc
  }
}
