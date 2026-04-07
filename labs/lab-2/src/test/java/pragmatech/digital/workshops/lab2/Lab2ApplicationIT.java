package pragmatech.digital.workshops.lab2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab2.config.WireMockContextInitializer;

@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Lab2ApplicationIT {

  @Test
  void contextLoads() {
    // This test verifies that the application context loads successfully
  }
}
