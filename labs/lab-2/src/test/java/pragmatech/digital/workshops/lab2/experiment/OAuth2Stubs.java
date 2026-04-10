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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Test helper that turns a plain {@link WireMockServer} into a fake OAuth2 / OIDC
 * identity provider so integration tests can skip Keycloak entirely.
 *
 * <p>Inspired by <a href=
 * "https://github.com/rieckpil/testing-spring-boot-applications-masterclass/blob/main/src/test/java/de/rieckpil/courses/stubs/OAuth2Stubs.java">
 * OAuth2Stubs.java from the testing-spring-boot-applications-masterclass</a>.
 *
 * <p>Once {@link #stubOpenIdConfiguration()} has been called, the server exposes:
 * <ul>
 *   <li>{@code GET /.well-known/openid-configuration} — minimal discovery document
 *       pointing back at the WireMock base URL for the JWKS endpoint.</li>
 *   <li>{@code GET /protocol/openid-connect/certs} — JWKS containing the public
 *       half of the locally generated RSA key pair.</li>
 * </ul>
 *
 * Tokens minted via {@link #signedJwt(String, String...)} are signed with the
 * matching private key, so Spring Security's {@code NimbusJwtDecoder} will accept
 * them after fetching the stubbed JWKS once.
 */
public class OAuth2Stubs {

  private final WireMockServer server;
  private final String realm;
  private final KeyPair keyPair;
  private final String keyId = UUID.randomUUID().toString();

  public OAuth2Stubs(WireMockServer server, String realm) {
    this.server = server;
    this.realm = realm;
    this.keyPair = generateRsaKeyPair();
  }

  /**
   * Base URL that should be configured as
   * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}. Matches the
   * {@code iss} claim of tokens returned by {@link #signedJwt(String, String...)}.
   */
  public String issuerUri() {
    return server.baseUrl() + "/realms/" + realm;
  }

  public void stubOpenIdConfiguration() {
    String issuer = issuerUri();
    String jwksUri = issuer + "/protocol/openid-connect/certs";

    String discovery = """
      {
        "issuer": "%s",
        "jwks_uri": "%s",
        "id_token_signing_alg_values_supported": ["RS256"],
        "subject_types_supported": ["public"],
        "response_types_supported": ["id_token", "token"]
      }
      """.formatted(issuer, jwksUri);

    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    String modulus = base64Url(publicKey.getModulus().toByteArray());
    String exponent = base64Url(publicKey.getPublicExponent().toByteArray());

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
      """.formatted(keyId, modulus, exponent);

    server.stubFor(WireMock.get("/realms/" + realm + "/.well-known/openid-configuration")
      .willReturn(WireMock.okJson(discovery)));
    server.stubFor(WireMock.get("/realms/" + realm + "/protocol/openid-connect/certs")
      .willReturn(WireMock.okJson(jwks)));
  }

  /**
   * Mint a signed JWT for {@code subject} with the given {@code scopes}. The token
   * mirrors the shape of a Keycloak access token: {@code iss}, {@code sub},
   * {@code iat}, {@code exp}, and a space-separated {@code scope} claim.
   */
  public String signedJwt(String subject, String... scopes) {
    try {
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(subject)
        .issuer(issuerUri())
        .issueTime(Date.from(Instant.now()))
        .expirationTime(Date.from(Instant.now().plusSeconds(300)))
        .claim("scope", String.join(" ", scopes))
        .jwtID(UUID.randomUUID().toString())
        .build();

      SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),
        claims);
      jwt.sign(new RSASSASigner((RSAPrivateKey) keyPair.getPrivate()));
      return jwt.serialize();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to mint signed JWT", e);
    }
  }

  private static KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to generate RSA key pair", e);
    }
  }

  private static String base64Url(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
