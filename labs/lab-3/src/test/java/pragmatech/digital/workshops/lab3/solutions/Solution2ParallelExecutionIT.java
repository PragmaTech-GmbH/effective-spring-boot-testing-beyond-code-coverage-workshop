package pragmatech.digital.workshops.lab3.solutions;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2 — Parallel-safe integration test.
 *
 * Configure {@code junit-platform.properties}:
 * <pre>
 * junit.jupiter.execution.parallel.enabled = true
 * junit.jupiter.execution.parallel.mode.classes.default = concurrent
 * junit.jupiter.execution.parallel.mode.default = same_thread
 * </pre>
 *
 * The test below is safe because:
 * <ul>
 *   <li>It uses a unique random ISBN per invocation — no shared row keys.</li>
 *   <li>It does not mutate any static state.</li>
 *   <li>It does not rely on {@code @DirtiesContext}.</li>
 *   <li>It shares the cached context (via {@link SharedIntegrationTestBase}) with
 *       every other parallel test.</li>
 * </ul>
 */
class Solution2ParallelExecutionIT extends SharedIntegrationTestBase {

  @Autowired
  BookRepository bookRepository;

  @Test
  void shouldRunSafelyAlongsideOtherIntegrationTests() {
    String uniqueIsbn = "979" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 10);

    Book saved = bookRepository.save(new Book(uniqueIsbn, "Parallel", "Safe", LocalDate.now().minusYears(1)));

    assertThat(bookRepository.findById(saved.getId())).isPresent();
  }
}
