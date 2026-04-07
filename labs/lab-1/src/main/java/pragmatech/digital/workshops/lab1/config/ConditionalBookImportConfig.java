package pragmatech.digital.workshops.lab1.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalBookImportConfig {

  @Bean
  @ConditionalOnProperty(name = "bookshelf.import.enabled", havingValue = "true")
  public String bookImportEnabled() {
    return "BookImportEnabled";
  }
}
