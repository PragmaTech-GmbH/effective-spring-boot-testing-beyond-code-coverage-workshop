package pragmatech.digital.workshops.lab2.event;

public record BookCreatedEvent(Long bookId, String isbn, String title) {}
