package pragmatech.digital.workshops.lab3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Forwards {@code /app} and {@code /app/} to the SPA entry point so users can
 * open the Bookshelf Admin View without typing {@code /app/index.html}.
 */
@Configuration
public class WebMvcAppConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/app").setViewName("forward:/app/index.html");
    registry.addViewController("/app/").setViewName("forward:/app/index.html");
  }
}
