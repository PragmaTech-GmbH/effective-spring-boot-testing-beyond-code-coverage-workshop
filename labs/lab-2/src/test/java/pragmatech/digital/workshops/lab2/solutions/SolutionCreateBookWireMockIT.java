package pragmatech.digital.workshops.lab2.solutions;

import java.time.LocalDate;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import pragmatech.digital.workshops.lab2.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Solution — Integration test for {@code POST /api/books} with a WireMock-stubbed
 * OpenLibrary API.
 *
 * <p>Key moving parts:
 * <ul>
 *   <li>A real PostgreSQL container via {@code @ServiceConnection}.</li>
 *   <li>A {@link WireMockExtension} serving as the fake OpenLibrary upstream, its
 *       dynamic base URL injected into {@code book.metadata.api.url} through
 *       {@code @DynamicPropertySource}.</li>
 *   <li>A {@link TestConfiguration} that overrides the default
 *       {@code FallbackOpenLibraryApiClient} bean with a real
 *       {@link OpenLibraryApiClient} (marked {@code @Primary}) so the service
 *       actually hits WireMock.</li>
 *   <li>A JWT forged with {@code jwt().authorities(...)} carrying the
 *       {@code SCOPE_books:write} authority required by {@code SecurityConfig} —
 *       no Keycloak needed.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(SolutionCreateBookWireMockIT.RealOpenLibraryClientConfig.class)
class SolutionCreateBookWireMockIT {

  private static final String ISBN = "978-0132350884";

  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @RegisterExtension
  static final WireMockExtension WIREMOCK = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void overrideOpenLibraryBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
  }

  @Autowired
  MockMvc mockMvc;

  @Autowired
  BookRepository bookRepository;

  @Test
  void shouldCreateBookAndEnrichMetadataFromOpenLibrary() throws Exception {
    WIREMOCK.stubFor(get(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(ISBN))
      .withQueryParam("jscmd", equalTo("data"))
      .withQueryParam("format", equalTo("json"))
      .willReturn(ok()
        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .withBody("""
          {
            "978-0132350884": {
              "title": "Clean Code",
              "authors": [ { "name": "Robert C. Martin" } ],
              "cover": { "small": "https://covers.openlibrary.org/b/id/8085499-S.jpg" }
            }
          }
          """)));

    String body = """
      {
        "isbn": "%s",
        "internalName": "clean-code-shelf-a3",
        "availabilityDate": "%s"
      }
      """.formatted(ISBN, LocalDate.now().plusDays(7));

    mockMvc.perform(post("/api/books")
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"));

    assertThat(bookRepository.findByIsbn(ISBN))
      .hasValueSatisfying(book -> {
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getThumbnailUrl())
          .isEqualTo("https://covers.openlibrary.org/b/id/8085499-S.jpg");
        assertThat(book.getInternalName()).isEqualTo("clean-code-shelf-a3");
      });

    WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(ISBN)));

    bookRepository.deleteAll();
  }

  /**
   * Swaps the default {@code FallbackOpenLibraryApiClient} bean for a real
   * {@link OpenLibraryApiClient} so the service actually performs HTTP calls
   * against the WireMock-stubbed upstream.
   */
  @TestConfiguration
  static class RealOpenLibraryClientConfig {

    @Bean
    @Primary
    OpenLibraryApiClient openLibraryApiClient(WebClient openLibraryWebClient) {
      return new OpenLibraryApiClient(openLibraryWebClient);
    }

    /**
     * Placeholder decoder so the OAuth2 resource server can wire up without a
     * real issuer. The {@code jwt()} MockMvc post-processor bypasses this
     * decoder entirely at request time.
     */
    @Bean
    JwtDecoder jwtDecoder() {
      return token -> {
        throw new UnsupportedOperationException(
          "Not used — MockMvc jwt() post-processor builds the Authentication directly");
      };
    }
  }
}
