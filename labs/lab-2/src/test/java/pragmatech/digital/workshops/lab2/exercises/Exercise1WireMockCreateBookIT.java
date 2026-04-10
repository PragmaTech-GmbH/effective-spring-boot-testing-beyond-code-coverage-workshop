package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 1 — Full integration test for {@code POST /api/books} with WireMock.
 *
 * {@code BookService.createBook} calls OpenLibrary to enrich book metadata
 * ({@code BookMetadataResponse#getCoverUrl}) and stores the resulting URL on the
 * persisted entity. Stub OpenLibrary with WireMock so the test runs offline and is
 * deterministic.
 *
 * Goal:
 * <ol>
 *   <li>Stub {@code GET /api/books/&lt;isbn&gt;} on the WireMock server with a
 *       canned JSON response that returns a {@code covers} array.</li>
 *   <li>Point {@code book.metadata.api.url} at the WireMock server via
 *       {@code @DynamicPropertySource}.</li>
 *   <li>Issue an authenticated {@code POST /api/books} with a fresh ISBN.</li>
 *   <li>Assert {@code 201 Created}, the persisted {@code thumbnailUrl} matches the
 *       stubbed value, and that WireMock received exactly one request to the
 *       expected path.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>Use {@code WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build()}
 *       — there is already a {@code WireMockContextInitializer} you can study.</li>
 *   <li>Stubs that already exist in {@code src/test/resources/__files/} can be
 *       served via {@code aResponse().withBodyFile("9780132350884-success.json")}.</li>
 *   <li>For auth use {@code SecurityMockMvcRequestPostProcessors.jwt().authorities(...)}.</li>
 * </ul>
 */
@SpringBootTest
@Disabled("TODO: implement Exercise 1")
class Exercise1WireMockCreateBookIT {

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() {
    // TODO
  }
}
