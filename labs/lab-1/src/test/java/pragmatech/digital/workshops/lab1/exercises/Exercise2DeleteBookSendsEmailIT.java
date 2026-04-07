package pragmatech.digital.workshops.lab1.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 2 — Verify the deletion notification email end-to-end.
 *
 * Building on Exercise 1, write a full integration test that:
 * <ol>
 *   <li>Seeds one book (insert directly via the repository, or via {@code POST /api/books}).</li>
 *   <li>Issues an authenticated {@code DELETE /api/books/{id}} and asserts
 *       {@code 204 No Content}.</li>
 *   <li>Polls Mailpit's HTTP API ({@code GET /api/v1/messages}) until exactly one
 *       message has been delivered.</li>
 *   <li>Asserts the subject starts with {@code "Book removed from library"} and the
 *       body contains the deleted book's ISBN.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>Use Awaitility to wait for the email — emails are sent asynchronously by
 *       {@code JavaMailSender} and need a few hundred ms to land.</li>
 *   <li>Reach the Mailpit HTTP API via {@code RestClient} or {@code WebClient}; the
 *       response is a JSON object with a {@code messages} array.</li>
 *   <li>For authentication use {@code SecurityMockMvcRequestPostProcessors.jwt()} —
 *       we'll cover real JWT signing in Lab 2.</li>
 * </ul>
 */
@SpringBootTest
@Disabled("TODO: implement Exercise 2")
class Exercise2DeleteBookSendsEmailIT {

  @Test
  void shouldSendDeletionEmailWhenBookIsDeleted() {
    // TODO
  }
}
