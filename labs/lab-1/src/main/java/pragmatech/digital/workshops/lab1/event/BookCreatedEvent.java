package pragmatech.digital.workshops.lab1.event;

public record BookCreatedEvent(Long bookId, String isbn, String title) {}
