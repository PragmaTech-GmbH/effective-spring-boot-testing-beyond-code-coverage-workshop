package pragmatech.digital.workshops.lab4.experiment;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import pragmatech.digital.workshops.lab4.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab4.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab4.service.BookNotificationService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@TestPropertySource(properties = {
  "spring.mail.host=localhost",
  "spring.mail.port=3025"
})
class BookNotificationServiceTest {

  @RegisterExtension
  static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
    .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
    .withPerMethodLifecycle(false);

  @Autowired
  private BookNotificationService bookNotificationService;

  @Test
  void shouldSendEmailWhenNotifyingAboutNewBook() throws Exception {
    bookNotificationService.notifyNewBook("Effective Java", "reader@example.com");

    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
    assertThat(receivedMessages).hasSize(1);

    MimeMessage message = receivedMessages[0];
    assertThat(message.getSubject()).isEqualTo("New book available: Effective Java");
    assertThat(message.getAllRecipients()[0].toString()).isEqualTo("reader@example.com");
  }
}
