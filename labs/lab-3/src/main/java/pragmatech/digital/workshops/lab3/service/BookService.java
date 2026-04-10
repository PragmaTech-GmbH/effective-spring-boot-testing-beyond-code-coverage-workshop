package pragmatech.digital.workshops.lab3.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient.BookMetadata;
import pragmatech.digital.workshops.lab3.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab3.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.exception.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab3.exception.BookMetadataUnavailableException;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

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

    BookMetadata metadata = openLibraryApiClient.fetchMetadataForIsbn(request.isbn())
      .orElseThrow(() -> new BookMetadataUnavailableException(request.isbn()));

    if (metadata.title() == null || metadata.author() == null) {
      logger.warn("OpenLibrary record for ISBN {} is missing title or author: {}", request.isbn(), metadata);
      throw new BookMetadataUnavailableException(request.isbn());
    }

    Book book = new Book(
      request.isbn(),
      request.internalName(),
      request.availabilityDate(),
      metadata.title(),
      metadata.author()
    );
    book.setThumbnailUrl(metadata.thumbnailUrl());

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
        book.setInternalName(request.internalName());
        book.setAvailabilityDate(request.availabilityDate());
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
