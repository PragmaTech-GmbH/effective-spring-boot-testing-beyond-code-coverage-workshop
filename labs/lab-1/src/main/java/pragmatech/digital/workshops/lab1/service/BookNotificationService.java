package pragmatech.digital.workshops.lab1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BookNotificationService {

  private final JavaMailSender mailSender;
  private final String fromAddress;

  public BookNotificationService(JavaMailSender mailSender,
      @Value("${bookshelf.notification.from:library@example.com}") String fromAddress) {
    this.mailSender = mailSender;
    this.fromAddress = fromAddress;
  }

  public void notifyNewBook(String title, String recipientEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientEmail);
    message.setSubject("New book available: " + title);
    message.setText("A new book has been added to the library: " + title);
    mailSender.send(message);
  }

  public void notifyDeletedBook(String title, String isbn, String recipientEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientEmail);
    message.setSubject("Book removed from library: " + title);
    message.setText("The following book has been removed from the library:\n\nTitle: " + title + "\nISBN: " + isbn);
    mailSender.send(message);
  }
}
