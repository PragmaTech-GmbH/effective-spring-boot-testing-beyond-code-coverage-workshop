package pragmatech.digital.workshops.lab3.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.entity.BookStatus;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

@RestController
@RequestMapping("/api/tests")
public class ThreadController {

  private final BookRepository bookRepository;

  public ThreadController(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @GetMapping("/thread-id")
  public String getThreadId() {
    return String.valueOf(Thread.currentThread().getId());
  }

  @GetMapping("/data-access/{isbn}")
  public ResponseEntity<Book> getBookForDataAccessTest(@PathVariable String isbn) {
    return bookRepository.findByIsbn(isbn)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/create-for-test/{isbn}/{title}")
  @Transactional
  public ResponseEntity<Book> createBookForTest(@PathVariable String isbn, @PathVariable String title) {
    Book book = new Book();
    book.setIsbn(isbn);
    book.setTitle(title);
    book.setAuthor("Test Author");
    book.setPublishedDate(LocalDate.now());
    book.setStatus(BookStatus.AVAILABLE);
    Book savedBook = bookRepository.save(book);
    return ResponseEntity.ok(savedBook);
  }
}
