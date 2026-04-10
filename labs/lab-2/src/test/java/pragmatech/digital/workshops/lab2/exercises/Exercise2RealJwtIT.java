package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercise 2 — Use a real, signed JWT (no Spring Security test mocks).
 *
 * In Lab 1 we authenticated via {@code SecurityMockMvcRequestPostProcessors.jwt()},
 * which only works with {@code MockMvc}. For a {@code RANDOM_PORT} integration test
 * — or to validate the actual {@code JwtDecoder} configuration — you need a token
 * the application accepts at runtime.
 *
 * Goal:
 * <ol>
 *   <li>Generate an RSA key pair in a {@code @TestConfiguration}.</li>
 *   <li>Override {@code JwtDecoder} with one that trusts your test public key.</li>
 *   <li>Build and sign a JWT (use {@code com.nimbusds.jose} — already on the
 *       classpath via Spring Security) with the claims your {@code SecurityConfig}
 *       expects.</li>
 *   <li>Send the token in the {@code Authorization} header of a real HTTP request
 *       and assert the response is {@code 201 Created}.</li>
 * </ol>
 *
 * Hints:
 * <ul>
 *   <li>{@code NimbusJwtDecoder.withPublicKey(publicKey).build()}.</li>
 *   <li>{@code JWSObject} with {@code RSASSASigner(privateKey)}.</li>
 *   <li>Don't forget the {@code iss} claim if your security config validates it.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("TODO: implement Exercise 2")
class Exercise2RealJwtIT {

  @Test
  void shouldAcceptSelfSignedJwtForAuthenticatedRequest() {
    // TODO
  }
}
