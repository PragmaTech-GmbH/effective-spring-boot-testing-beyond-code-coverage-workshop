package pragmatech.digital.workshops.lab3.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;


@Service
public class BookNotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(BookNotificationService.class);

  private final JavaMailSender mailSender;
  private final Configuration freemarkerConfiguration;
  private final String fromAddress;

  public BookNotificationService(
    JavaMailSender mailSender,
    Configuration freemarkerConfiguration,
    @Value("${bookshelf.notification.from:library@example.com}") String fromAddress) {
    this.mailSender = mailSender;
    this.freemarkerConfiguration = freemarkerConfiguration;
    this.fromAddress = fromAddress;
  }

  public void notifyDeletedBook(String title, String isbn, String recipientEmail) {
    try {
      String htmlBody = renderDeletedBookTemplate(title, isbn);

      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
      helper.setFrom(fromAddress);
      helper.setTo(recipientEmail);
      helper.setSubject("Book removed from library: " + title);
      helper.setText(htmlBody, true);

      mailSender.send(mimeMessage);
    }
    catch (MessagingException | IOException | TemplateException exception) {
      LOG.warn("Failed to send deletion notification email for book isb={}", isbn, exception);
    }
  }

  private String renderDeletedBookTemplate(String title, String isbn) throws IOException, TemplateException {
    Template template = freemarkerConfiguration.getTemplate("mail/book-deleted.ftlh");
    Map<String, Object> templateModel = Map.of(
      "title", title,
      "isbn", isbn
    );
    return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateModel);
  }
}
