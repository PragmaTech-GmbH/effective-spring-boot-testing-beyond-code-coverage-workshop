package pragmatech.digital.workshops.lab2.experiment;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Experiment — Reproduce the Keycloak flow without Keycloak.
 *
 * Instead of starting a real Identity Provider container, we:
 * <ol>
 *   <li>Generate an RSA key pair locally.</li>
 *   <li>Stand up a WireMock server pretending to be an OAuth2 issuer:
 *     <ul>
 *       <li>{@code GET /.well-known/openid-configuration} → returns the discovery
 *           document pointing at our fake JWKS endpoint.</li>
 *       <li>{@code GET /protocol/openid-connect/certs} → returns a JWKS containing
 *           the public key.</li>
 *     </ul>
 *   </li>
 *   <li>Sign tokens with the matching private key and send them to the application.
 *       Spring Security's {@code NimbusJwtDecoder} fetches our stubbed JWKS exactly
 *       once, caches the public key, and then verifies every token offline.</li>
 * </ol>
 *
 * Trade-off vs. {@code AbstractOAuth2IntegrationTest}:
 * <ul>
 *   <li>✅ Much faster: no Keycloak boot time (~10s).</li>
 *   <li>✅ Hermetic: no Docker pull, no IdP version coupling.</li>
 *   <li>❌ You're not exercising the real IdP — token shape mismatches won't be
 *       caught until staging.</li>
 * </ul>
 *
 * See {@code slides/assets/lab-2-jwks-stub-flow.png} for the sequence diagram.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Superseded by BookControllerOAuth2StubIT + OAuth2Stubs helper")
class WireMockJwksAuthIT {

  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");
  static {
    POSTGRES.start();
  }

  @ServiceConnection
  static PostgreSQLContainer postgres() {
    return POSTGRES;
  }

  static final WireMockServer ISSUER = new WireMockServer(WireMockConfiguration.options().dynamicPort());
  static final KeyPair KEY_PAIR;
  static final String KID = UUID.randomUUID().toString();

  static {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      KEY_PAIR = gen.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @BeforeAll
  static void startIssuer() {
    ISSUER.start();
    String issuerUrl = ISSUER.baseUrl();

    String discovery = """
      {
        "issuer": "%s",
        "jwks_uri": "%s/protocol/openid-connect/certs",
        "id_token_signing_alg_values_supported": ["RS256"],
        "subject_types_supported": ["public"],
        "response_types_supported": ["id_token"]
      }
      """.formatted(issuerUrl, issuerUrl);

    RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
    String n = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
    String e = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

    String jwks = """
      {
        "keys": [
          {
            "kty": "RSA",
            "use": "sig",
            "alg": "RS256",
            "kid": "%s",
            "n": "%s",
            "e": "%s"
          }
        ]
      }
      """.formatted(KID, n, e);

    ISSUER.stubFor(WireMock.get("/.well-known/openid-configuration")
      .willReturn(WireMock.okJson(discovery)));
    ISSUER.stubFor(WireMock.get("/protocol/openid-connect/certs")
      .willReturn(WireMock.okJson(jwks)));
  }

  @AfterAll
  static void stopIssuer() {
    ISSUER.stop();
  }

  @DynamicPropertySource
  static void issuerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", ISSUER::baseUrl);
  }

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void shouldAcceptTokenSignedWithStubbedJwksKey() throws Exception {
    String token = signedJwt("alice", "books:read books:write");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> response = restTemplate.exchange(
      "/api/books/1",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldRejectTokenSignedWithDifferentKey() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair otherKey = gen.generateKeyPair();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
      .subject("evil")
      .issuer(ISSUER.baseUrl())
      .issueTime(Date.from(Instant.now()))
      .expirationTime(Date.from(Instant.now().plusSeconds(60)))
      .claim("scope", "books:write")
      .build();

    SignedJWT jwt = new SignedJWT(
      new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KID).build(),
      claims);
    jwt.sign(new RSASSASigner((RSAPrivateKey) otherKey.getPrivate()));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwt.serialize());

    ResponseEntity<String> response = restTemplate.exchange(
      "/api/books/1",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  private static String signedJwt(String subject, String scope) throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
      .subject(subject)
      .issuer(ISSUER.baseUrl())
      .issueTime(Date.from(Instant.now()))
      .expirationTime(Date.from(Instant.now().plusSeconds(300)))
      .claim("scope", scope)
      .build();

    SignedJWT jwt = new SignedJWT(
      new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KID).build(),
      claims);
    jwt.sign(new RSASSASigner((RSAPrivateKey) KEY_PAIR.getPrivate()));
    return jwt.serialize();
  }
}
