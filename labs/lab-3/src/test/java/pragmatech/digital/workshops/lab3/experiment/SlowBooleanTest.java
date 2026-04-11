package pragmatech.digital.workshops.lab3.experiment;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dummy test class #4 — artificially slow boolean checks.
 *
 * <p>Every test sleeps for a random period and then asserts something
 * that obviously holds. Exists solely to give the parallel-execution
 * experiment enough work to spread across threads.
 */
@DisplayName("Slow boolean checks (dummy)")
class SlowBooleanTest {

  @Test
  void shouldBeTrueAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(150, 650));
    assertThat(true).isTrue();
  }

  @Test
  void shouldBeFalseAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(150, 650));
    assertThat(false).isFalse();
  }

  @Test
  void shouldEvaluateAndAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(150, 650));
    assertThat(true && false).isFalse();
  }

  @Test
  void shouldEvaluateOrAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(150, 650));
    assertThat(true || false).isTrue();
  }
}
