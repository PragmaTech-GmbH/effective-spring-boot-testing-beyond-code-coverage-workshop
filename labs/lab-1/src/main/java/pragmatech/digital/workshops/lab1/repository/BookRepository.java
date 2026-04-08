package pragmatech.digital.workshops.lab1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import pragmatech.digital.workshops.lab1.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

  /**
   * Find a book by its ISBN.
   *
   * @param isbn the ISBN to search for
   * @return the book with the given ISBN, if found
   */
  Optional<Book> findByIsbn(String isbn);
}
