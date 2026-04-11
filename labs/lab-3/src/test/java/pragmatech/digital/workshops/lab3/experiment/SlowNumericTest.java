package pragmatech.digital.workshops.lab3.experiment;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dummy test class #5 — artificially slow numeric range checks.
 *
 * <p>Rounds out the set of five dummy classes used to demonstrate JUnit
 * parallel execution. Nothing meaningful is tested — the {@code Thread.sleep}
 * calls are the whole point.
 */
@DisplayName("Slow numeric range checks (dummy)")
class SlowNumericTest {

  @Test
  void shouldBePositiveAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 700));
    assertThat(42).isPositive();
  }

  @Test
  void shouldBeNegativeAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 700));
    assertThat(-7).isNegative();
  }

  @Test
  void shouldBeWithinRangeAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 700));
    assertThat(50).isBetween(1, 100);
  }

  @Test
  void shouldBeZeroAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 700));
    assertThat(0).isZero();
  }
}
