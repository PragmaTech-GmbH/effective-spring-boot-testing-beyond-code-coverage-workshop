package pragmatech.digital.workshops.lab4.event;

public record BookCreatedEvent(Long bookId, String isbn, String title) {}
