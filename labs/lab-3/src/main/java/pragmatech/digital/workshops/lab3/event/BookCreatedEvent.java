package pragmatech.digital.workshops.lab3.event;

public record BookCreatedEvent(Long bookId, String isbn, String title) {}
