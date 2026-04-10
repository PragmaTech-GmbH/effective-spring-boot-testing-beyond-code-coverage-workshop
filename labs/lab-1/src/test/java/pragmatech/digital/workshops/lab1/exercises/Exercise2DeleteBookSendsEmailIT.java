package pragmatech.digital.workshops.lab1.exercises;

import pragmatech.digital.workshops.lab1.config.AbstractOAuth2IntegrationTest;

/**
 * Exercise 2 — Integration test for the "delete a book" flow.
 *
 * <h2>Your task</h2>
 * Write an end-to-end integration test that exercises {@code DELETE /api/books/{id}} and
 * verifies that:
 * <ol>
 *   <li>The endpoint returns {@code 204 No Content}.</li>
 *   <li>The book is really gone from Postgres afterwards
 *       (e.g. {@code bookRepository.findById(id)} is empty).</li>
 * </ol>
 *
 * <h3>Steps to get started</h3>
 * <ul>
 *   <li>Extend {@link AbstractOAuth2IntegrationTest} — it boots Postgres + Keycloak
 *       Testcontainers and wires {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
 *       for you.</li>
 *   <li>Inject {@code TestRestTemplate} and {@code BookRepository}.</li>
 *   <li><b>Arrange a book</b> — either:
 *     <ul>
 *       <li>Pre-insert one directly via {@code bookRepository.save(new Book(...))}, <i>or</i></li>
 *       <li>Create one through the real {@code POST /api/books} endpoint to cover the
 *           full creation + deletion flow.</li>
 *     </ul>
 *   </li>
 *   <li><b>Authenticate</b> — the delete endpoint requires {@code books:write}. Fetch a bearer
 *       token via {@link AbstractOAuth2IntegrationTest#fetchPasswordGrantToken(String, String)}
 *       using the seeded {@code admin/admin} (or {@code bob/bob}) user, and send it via the
 *       {@code Authorization} header (use {@code bearerHeaders(token)}).</li>
 *   <li>Assert on the response status and on the repository state.</li>
 * </ul>
 *
 * <h3>Optional stretch goal — verify the email was sent</h3>
 * Deleting a book triggers {@code BookNotificationService#notifyDeletedBook}, which renders
 * a FreeMarker template and sends an HTML mail. Brainstorm how you would <i>meaningfully</i>
 * verify this in a test — a few ideas worth discussing:
 *
 * <ol>
 *   <li><b>Mailpit as a black-box SMTP sink.</b> Start a {@code MailpitContainer} alongside
 *       Postgres/Keycloak, override {@code spring.mail.host} / {@code spring.mail.port} via
 *       {@code @DynamicPropertySource}, then poll Mailpit's REST API
 *       ({@code GET /api/v1/messages}) with Awaitility until a message shows up. You can
 *       assert on: recipient, subject (should start with "Book removed from library"),
 *       and the HTML body (should contain the title and ISBN of the deleted book).
 *       This is the most realistic check — it proves the SMTP protocol + rendered template
 *       actually work end-to-end.</li>
 *
 *   <li><b>{@code @SpyBean} / Mockito spy on {@code BookNotificationService}</b> and verify
 *       {@code notifyDeletedBook(title, isbn, recipient)} was called with the right arguments.
 *       Fast and focused, but only proves <i>the method was invoked</i> — not that the
 *       template renders, SMTP connects, or the mail server accepts it.</li>
 *
 *   <li><b>{@code GreenMail} embedded in-process.</b> No Docker needed, gives you full access
 *       to the received MimeMessage so you can assert on headers and HTML body content.
 *       Good middle ground between speed and realism.</li>
 *
 *   <li><b>Capture the rendered body directly.</b> Inject a test-only {@code JavaMailSender}
 *       stub that captures the {@code MimeMessage} and lets the test assert on the rendered
 *       HTML. Validates the template but not the transport.</li>
 * </ol>
 *
 * <p><b>Recommendation for this exercise:</b> start with option (1) — it's the closest thing
 * to production and shows off Testcontainers nicely. Option (2) is a good "quick win" you can
 * layer on top if the Mailpit round-trip feels too slow.
 *
 * <h3>Reference</h3>
 * A fully worked solution lives in
 * {@code pragmatech.digital.workshops.lab1.solutions.Solution2DeleteBookSendsEmailIT} —
 * peek only after you've had a go yourself. ✌️
 */
class Exercise2DeleteBookSendsEmailIT {

  // TODO: implement the integration test described above.

}
