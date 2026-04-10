package pragmatech.digital.workshops.lab4.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a book in the library system.
 */
@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String isbn;

  @Column(name = "internal_name", nullable = false)
  private String internalName;

  @Column(name = "availability_date", nullable = false)
  private LocalDate availabilityDate;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column
  private String description;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookStatus status = BookStatus.AVAILABLE;

  public Book() {
  }

  public Book(String isbn, String internalName, LocalDate availabilityDate, String title, String author) {
    this.isbn = isbn;
    this.internalName = internalName;
    this.availabilityDate = availabilityDate;
    this.title = title;
    this.author = author;
    this.status = BookStatus.AVAILABLE;
  }

  public Long getId() {
    return id;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getInternalName() {
    return internalName;
  }

  public void setInternalName(String internalName) {
    this.internalName = internalName;
  }

  public LocalDate getAvailabilityDate() {
    return availabilityDate;
  }

  public void setAvailabilityDate(LocalDate availabilityDate) {
    this.availabilityDate = availabilityDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public BookStatus getStatus() {
    return status;
  }

  public void setStatus(BookStatus status) {
    this.status = status;
  }

  public boolean isAvailable() {
    return status == BookStatus.AVAILABLE;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Book book = (Book) o;
    if (id != null) {
      return id.equals(book.id);
    }
    return isbn.equals(book.isbn);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : isbn.hashCode();
  }

  @Override
  public String toString() {
    return "Book{" +
      "id=" + id +
      ", isbn='" + isbn + '\'' +
      ", internalName='" + internalName + '\'' +
      ", availabilityDate=" + availabilityDate +
      ", title='" + title + '\'' +
      ", author='" + author + '\'' +
      ", status=" + status +
      '}';
  }
}
