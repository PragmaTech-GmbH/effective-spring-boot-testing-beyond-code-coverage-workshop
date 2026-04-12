package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise — Integration test for {@code POST /api/books} with WireMock-stubbed OpenLibrary.
 *
 * <h2>Your task</h2>
 * Write a {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} integration test that:
 * <ol>
 *   <li>Starts a <b>PostgreSQL</b> Testcontainer ({@code @ServiceConnection})</li>
 *   <li>Starts a <b>WireMockServer</b> and stubs {@code GET /api/books?bibkeys=<isbn>&jscmd=data&format=json}
 *       with a canned JSON response containing title, author, and thumbnail URL</li>
 *   <li>Wires the WireMock base URL into the app via
 *       {@code @DynamicPropertySource} → {@code book.metadata.api.url}</li>
 *   <li>Provides a valid <b>signed JWT</b> with {@code SCOPE_books:write} — use {@code OAuth2Stubs}
 *       on the same WireMock instance to stub OIDC discovery + JWKS</li>
 *   <li>Sends {@code POST /api/books} with a valid {@code BookCreationRequest} JSON body
 *       and the Bearer token</li>
 *   <li>Asserts the response is {@code 201 Created} with a {@code Location} header</li>
 *   <li>Verifies the persisted book has the enriched metadata from WireMock
 *       (title, author, thumbnailUrl) via {@code BookRepository}</li>
 *   <li>Cleans up the database in {@code @AfterEach} — {@code @Transactional} rollback
 *       does <b>not</b> work with {@code RANDOM_PORT} because the server runs on a separate thread</li>
 * </ol>
 *
 * <h2>Hints</h2>
 * <ul>
 *   <li>Look at {@code experiment/OAuth2Stubs} for minting signed JWTs without Keycloak</li>
 *   <li>Look at {@code experiment/OpenLibraryStubs} for a helper that wraps the WireMock stubbing DSL</li>
 *   <li>The {@code FallbackOpenLibraryApiClient} is the default bean — override it with a real
 *       {@code OpenLibraryApiClient} via a {@code @TestConfiguration} + {@code @Primary} bean</li>
 *   <li>Use {@code RestTestClient} (via {@code @AutoConfigureRestTestClient}) for HTTP requests</li>
 *   <li>Reference solution: {@code solutions/SolutionCreateBookWireMockIT}</li>
 * </ul>
 */
class ExerciseCreateBookWireMockIT {

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() {
    // TODO: implement the integration test described above
  }
}
