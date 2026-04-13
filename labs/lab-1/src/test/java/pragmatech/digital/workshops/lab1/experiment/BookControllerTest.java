package pragmatech.digital.workshops.lab1.experiment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pragmatech.digital.workshops.lab1.config.SecurityConfig;
import pragmatech.digital.workshops.lab1.controller.BookController;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.service.BookService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  BookService bookService;

  @Test
  void shouldReturnAllBooksWithoutAuthentication() throws Exception {
    when(bookService.getAllBooks()).thenReturn(List.of(
      new Book("978-0132350884", "clean-code", LocalDate.of(2024, 1, 1), "Clean Code", "Robert C. Martin")
    ));

    mockMvc.perform(get("/api/books"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].isbn").value("978-0132350884"))
      .andExpect(jsonPath("$[0].title").value("Clean Code"));
  }

  @Test
  void shouldReturnBookByIdWhenAuthenticated() throws Exception {
    Book book = new Book("978-0132350884", "clean-code", LocalDate.of(2024, 1, 1), "Clean Code", "Robert C. Martin");
    when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

    mockMvc.perform(get("/api/books/{id}", 1L)
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.title").value("Clean Code"))
      .andExpect(jsonPath("$.author").value("Robert C. Martin"));
  }

  @Test
  void shouldReturn404WhenBookNotFound() throws Exception {
    when(bookService.getBookById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/books/{id}", 999L)
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
      .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn401WhenNoTokenProvidedForProtectedEndpoint() throws Exception {
    mockMvc.perform(get("/api/books/{id}", 1L))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn403WhenTokenMissingRequiredScope() throws Exception {
    mockMvc.perform(post("/api/books")
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read")))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "isbn": "978-0132350884",
            "internalName": "clean-code",
            "availabilityDate": "2024-01-01"
          }
          """))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldCreateBookWhenAuthorized() throws Exception {
    when(bookService.createBook(any())).thenReturn(42L);

    mockMvc.perform(post("/api/books")
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write")))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "isbn": "978-0132350884",
            "internalName": "clean-code",
            "availabilityDate": "2024-01-01"
          }
          """))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "http://localhost/api/books/42"));
  }

  @Test
  void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
    mockMvc.perform(post("/api/books")
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write")))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "isbn": "",
            "internalName": "",
            "availabilityDate": null
          }
          """))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDeleteBookWhenAuthorized() throws Exception {
    when(bookService.deleteBook(1L)).thenReturn(true);

    mockMvc.perform(delete("/api/books/{id}", 1L)
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write"))))
      .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn404WhenDeletingNonExistentBook() throws Exception {
    when(bookService.deleteBook(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/books/{id}", 999L)
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write"))))
      .andExpect(status().isNotFound());
  }

}
