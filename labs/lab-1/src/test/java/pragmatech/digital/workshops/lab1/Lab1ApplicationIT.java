package pragmatech.digital.workshops.lab1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab1.config.WireMockContextInitializer;

@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Lab1ApplicationIT {

  @Test
  void contextLoads() {
    // This test verifies that the application context loads successfully
  }
}
