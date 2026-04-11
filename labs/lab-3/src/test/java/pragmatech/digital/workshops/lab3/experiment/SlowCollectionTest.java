package pragmatech.digital.workshops.lab3.experiment;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dummy test class #3 — artificially slow collection checks.
 *
 * <p>Just enough code to look like real tests, with random sleeps so that
 * running the whole test class serially takes measurably longer than
 * running it in parallel.
 */
@DisplayName("Slow collection checks (dummy)")
class SlowCollectionTest {

  @Test
  void shouldContainElementAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(250, 750));
    assertThat(List.of("a", "b", "c")).contains("b");
  }

  @Test
  void shouldHaveExpectedSizeAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(250, 750));
    assertThat(Set.of(1, 2, 3, 4, 5)).hasSize(5);
  }

  @Test
  void shouldNotBeEmptyAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(250, 750));
    assertThat(List.of("spring", "boot")).isNotEmpty();
  }

  @Test
  void shouldDetectDuplicatesAfterRandomDelay() throws InterruptedException {
    Thread.sleep(ThreadLocalRandom.current().nextInt(250, 750));
    assertThat(Set.copyOf(List.of("a", "a", "b"))).hasSize(2);
  }
}
