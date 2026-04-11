package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerMockMvcIT {

  private static final String ISBN = "978-0132350884";

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

  static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static final OpenLibraryStubs openLibraryStubs;

  static {
    POSTGRES.start();
    WIREMOCK.start();
    openLibraryStubs = new OpenLibraryStubs(WIREMOCK);
  }

  @TestConfiguration
  public static class StubConfig {

    /**
     * The {@code jwt()} MockMvc post-processor builds the Authentication
     * directly — the actual decoder is never invoked. But Spring Security's
     * resource server autoconfiguration still requires a JwtDecoder bean to
     * exist, so we supply one that would throw if anyone ever called it.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
      return token -> {
        throw new UnsupportedOperationException(
          "Not used — the MockMvc jwt() post-processor builds the Authentication directly");
      };
    }
  }

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    // Point the real OpenLibraryApiClient at WireMock's base URL.
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
  }

  @Autowired
  MockMvc mockMvc;

  @Autowired
  BookRepository bookRepository;

  @Test
  @DisplayName("creates a book and returns 201 + Location when metadata is known")
  void shouldCreateBookWhenMetadataIsKnown() throws Exception {
    openLibraryStubs.stubMetadata(ISBN,
      "Clean Code",
      "Robert C. Martin",
      "https://covers.openlibrary.org/b/id/8085499-S.jpg");

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

    // Same-thread, same-transaction read: the row exists because the test
    // transaction can see writes performed inside the controller call.
    // When the test method exits, Spring rolls back and the row vanishes.
    assertThat(bookRepository.findByIsbn(ISBN))
      .hasValueSatisfying(book -> {
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getInternalName()).isEqualTo("clean-code-shelf-a3");
      });
  }

  @Test
  @DisplayName("returns 200 + book payload when ID exists and scope is books:read")
  void shouldReturnBookWhenAuthenticated() throws Exception {
    Book seeded = bookRepository.save(new Book(
      ISBN,
      "clean-code-shelf-a3",
      LocalDate.of(2024, 1, 1),
      "Clean Code",
      "Robert C. Martin"));

    mockMvc.perform(get("/api/books/{id}", seeded.getId())
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isbn").value(ISBN))
      .andExpect(jsonPath("$.title").value("Clean Code"))
      .andExpect(jsonPath("$.author").value("Robert C. Martin"));
  }

  @Test
  @DisplayName("returns 404 when the ID does not exist")
  void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    mockMvc.perform(get("/api/books/{id}", 999_999L)
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
      .andExpect(status().isNotFound());
  }
}
