package pragmatech.digital.workshops.lab3.experiment;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dummy test class #1 — artificially slow arithmetic.
 *
 * <p>The actual assertions are trivial; the whole point is to sleep for a
 * random amount of time on each method so that parallel execution has
 * something to schedule. Used in the JUnit parallel execution demo in the
 * lab-3 slides.
 */
@DisplayName("Slow arithmetic (dummy)")
class SlowArithmeticTest {

  @Test
  void shouldAddTwoNumbersAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 800));
    assertThat(1 + 1).isEqualTo(2);
  }

  @Test
  void shouldSubtractTwoNumbersAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 800));
    assertThat(10 - 4).isEqualTo(6);
  }

  @Test
  void shouldMultiplyTwoNumbersAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 800));
    assertThat(3 * 7).isEqualTo(21);
  }

  @Test
  void shouldDivideTwoNumbersAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 800));
    assertThat(42 / 6).isEqualTo(7);
  }
}
