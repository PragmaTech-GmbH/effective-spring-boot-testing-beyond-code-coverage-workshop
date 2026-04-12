package pragmatech.digital.workshops.lab1.exercises;

/**
 * Exercise — End-to-end integration test for the "delete a book and send notification email" flow.
 *
 * <h2>Your task</h2>
 * Write an integration test that:
 * <ol>
 *   <li>Boots the full application context with {@code @SpringBootTest(webEnvironment = RANDOM_PORT)}</li>
 *   <li>Starts three Testcontainers: <b>PostgreSQL</b> ({@code @ServiceConnection}),
 *       <b>Keycloak</b> (for OAuth2 JWTs), and <b>Mailpit</b> (fake SMTP server)</li>
 *   <li>Seeds a book into the database (via {@code BookRepository.save()} or {@code @Sql})</li>
 *   <li>Sends {@code DELETE /api/books/{id}} with a valid Bearer token from Keycloak</li>
 *   <li>Asserts the response status is {@code 204 No Content}</li>
 *   <li>Verifies the book is gone from Postgres ({@code bookRepository.findById(id).isEmpty()})</li>
 *   <li><b>Bonus:</b> Polls Mailpit's REST API ({@code GET /api/v1/search?query=<isbn>})
 *       to verify the deletion notification email was actually delivered</li>
 * </ol>
 *
 * <h2>Hints</h2>
 * <ul>
 *   <li>Look at the {@code experiment} package for {@code KeycloakContainer} and {@code MailpitContainer} helpers</li>
 *   <li>Use {@code @DynamicPropertySource} to wire {@code spring.security.oauth2.resourceserver.jwt.issuer-uri},
 *       {@code spring.mail.host}, and {@code spring.mail.port}</li>
 *   <li>Keycloak provides tokens via {@code KEYCLOAK.getAccessToken("admin", "admin")}</li>
 *   <li>Use {@code RestTestClient} (via {@code @AutoConfigureRestTestClient}) to fire HTTP requests</li>
 *   <li>For the email assertion, use {@code Awaitility.await()} since email delivery is asynchronous</li>
 *   <li>Reference solution: {@code solutions/SolutionDeleteBookSendsEmailIT}</li>
 * </ul>
 */
class ExerciseDeleteBookSendsEmailIT {

  // TODO: implement the integration test described above.

}
