package pragmatech.digital.workshops.lab1.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab1.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab1.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab1.dto.BookMetadataResponse;
import pragmatech.digital.workshops.lab1.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.exception.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab1.repository.BookRepository;

@Service
public class BookService {

  private static final Logger logger = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;
  private final OpenLibraryApiClient openLibraryApiClient;
  private final BookNotificationService bookNotificationService;
  private final String deletionNotificationRecipient;

  public BookService(BookRepository bookRepository,
    OpenLibraryApiClient openLibraryApiClient,
    BookNotificationService bookNotificationService,
    @Value("${bookshelf.notification.deletion-recipient:librarian@example.com}") String deletionNotificationRecipient) {
    this.bookRepository = bookRepository;
    this.openLibraryApiClient = openLibraryApiClient;
    this.bookNotificationService = bookNotificationService;
    this.deletionNotificationRecipient = deletionNotificationRecipient;
  }

  public Long createBook(BookCreationRequest request) {
    if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
      throw new BookAlreadyExistsException(request.isbn());
    }

    Book book = new Book(
      request.isbn(),
      request.title(),
      request.author(),
      request.publishedDate()
    );

    BookMetadataResponse metadata = openLibraryApiClient.getBookByIsbn(request.isbn());

    book.setThumbnailUrl(metadata.getCoverUrl());

    Book savedBook = bookRepository.save(book);

    return savedBook.getId();
  }

  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  public Optional<Book> getBookById(Long id) {
    return bookRepository.findById(id);
  }

  public Optional<Book> updateBook(Long id, BookUpdateRequest request) {
    return bookRepository.findById(id)
      .map(book -> {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPublishedDate(request.publishedDate());
        book.setStatus(request.status());
        return bookRepository.save(book);
      });
  }

  public boolean deleteBook(Long id) {
    return bookRepository.findById(id)
      .map(book -> {
        bookRepository.delete(book);
        bookNotificationService.notifyDeletedBook(book.getTitle(), book.getIsbn(), deletionNotificationRecipient);
        return true;
      })
      .orElse(false);
  }
}
