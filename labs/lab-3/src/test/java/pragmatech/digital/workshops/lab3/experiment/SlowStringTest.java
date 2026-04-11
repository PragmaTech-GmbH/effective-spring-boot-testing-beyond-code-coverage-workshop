package pragmatech.digital.workshops.lab3.experiment;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dummy test class #2 — artificially slow string manipulation.
 *
 * <p>No production code is actually exercised; each test sleeps for a
 * random duration and then asserts something obvious. Useful as ballast
 * for parallel execution experiments.
 */
@DisplayName("Slow string manipulation (dummy)")
class SlowStringTest {

  @Test
  void shouldUppercaseAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(300, 900));
    assertThat("spring".toUpperCase()).isEqualTo("SPRING");
  }

  @Test
  void shouldReverseAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(300, 900));
    assertThat(new StringBuilder("boot").reverse().toString()).isEqualTo("toob");
  }

  @Test
  void shouldConcatenateAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(300, 900));
    assertThat("foo" + "bar").isEqualTo("foobar");
  }

  @Test
  void shouldSplitAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(300, 900));
    assertThat("one,two,three".split(",")).hasSize(3);
  }
}
