package pragmatech.digital.workshops.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab1.experiment.KeycloakContainer;
import pragmatech.digital.workshops.lab1.experiment.MailpitContainer;

/**
 * Local development Testcontainers configuration.
 *
 * <p>Start the application with this configuration (via {@code TestLab1Application}) to run the
 * full backing stack — Postgres and a preconfigured Keycloak — inside Docker without having to
 * touch {@code compose.yaml}. The audience can see how Testcontainers powers local dev, not just
 * integration tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainersConfig {

  private static final Logger LOG = LoggerFactory.getLogger(LocalDevTestcontainersConfig.class);

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgres() {
    return new PostgreSQLContainer("postgres:16-alpine");
  }

  @Bean
  KeycloakContainer keycloak() {
    return new KeycloakContainer();
  }

  @Bean
  MailpitContainer mailpit() {
    return new MailpitContainer();
  }

  @Bean
  public ApplicationRunner notifyWebUI(MailpitContainer mailpitContainer) {
    return args -> LOG.info("Mailpit UI is accessible through http://localhost:{}", mailpitContainer.getHttpPort());
  }

  @Bean
  JavaMailSender javaMailSender(MailpitContainer mailpitContainer) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(mailpitContainer.getHost());
    sender.setPort(mailpitContainer.getSmtpPort());
    return sender;
  }

  @Bean
  DynamicPropertyRegistrar keycloakProperties(KeycloakContainer keycloak, MailpitContainer mailpitContainer) {
    return registry -> {
      registry.add(
        "spring.security.oauth2.resourceserver.jwt.issuer-uri",
        keycloak::getIssuerUri);
      registry.add("spring.mail.host", mailpitContainer::getHost);
      registry.add("spring.mail.port", mailpitContainer::getSmtpPort);
    };
  }
}
