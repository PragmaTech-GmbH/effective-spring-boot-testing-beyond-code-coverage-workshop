package pragmatech.digital.workshops.lab3.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

public class WireMockContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final Logger logger = LoggerFactory.getLogger(WireMockContextInitializer.class);

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMockServer.start();

    // Register a shutdown hook to stop WireMock when the context is closed
    applicationContext.addApplicationListener(event -> {
      if (event instanceof ContextClosedEvent) {
        logger.info("Stopping WireMock server");
        wireMockServer.stop();
      }
    });

    // Configure the stubs
    OpenLibraryApiStub openLibraryStub = new OpenLibraryApiStub(wireMockServer);

    openLibraryStub.stubForSuccessfulBookResponse("9780134757599");
    openLibraryStub.stubForSuccessfulBookResponse("9780201633610");
    openLibraryStub.stubForSuccessfulBookResponse("9780132350884");

    // Dashed-format stubs for BookCreationRequest (pattern: 978-XXXXXXXXXX)
    openLibraryStub.stubForSuccessfulBookResponseWithFile("978-0134757599", "9780134757599");
    openLibraryStub.stubForSuccessfulBookResponseWithFile("978-0201633610", "9780201633610");
    openLibraryStub.stubForSuccessfulBookResponseWithFile("978-0132350884", "9780132350884");

    applicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);
    applicationContext.getBeanFactory().registerSingleton("openLibraryStub", openLibraryStub);


    // Set the WebClient base URL property to point to our WireMock server
    TestPropertyValues.of(
      "book.metadata.api.url=http://localhost:" + wireMockServer.port()
    ).applyTo(applicationContext);

    logger.info("WireMock initialized on port {}", wireMockServer.port());
  }
}
