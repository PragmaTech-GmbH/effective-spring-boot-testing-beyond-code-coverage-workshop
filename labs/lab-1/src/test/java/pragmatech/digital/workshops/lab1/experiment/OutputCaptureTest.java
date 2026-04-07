package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class OutputCaptureTest {

  private static final Logger log = LoggerFactory.getLogger(OutputCaptureTest.class);

  @Test
  void shouldCaptureStdOutWhenPrintingToSystemOut(CapturedOutput output) {
    System.out.println("hello");

    assertThat(output.getOut()).contains("hello");
  }

  @Test
  void shouldCaptureLogOutputWhenLoggingWithSlf4j(CapturedOutput output) {
    log.info("Book created");

    assertThat(output.getAll()).contains("Book created");
  }
}
