package pragmatech.digital.workshops.lab2.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/api/books").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/books/*").hasAuthority("SCOPE_books:read")
        .requestMatchers(HttpMethod.POST, "/api/books").hasAuthority("SCOPE_books:write")
        .requestMatchers(HttpMethod.PUT, "/api/books/*").hasAuthority("SCOPE_books:write")
        .requestMatchers(HttpMethod.DELETE, "/api/books/*").hasAuthority("SCOPE_books:write")
        .requestMatchers("/api/tests/*").hasAuthority("SCOPE_books:write")
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  /**
   * Combines two sources of authorities for every JWT we accept:
   * <ul>
   *   <li>Standard {@code scope}/{@code scp} claim → {@code SCOPE_*} authorities (default Spring behaviour).</li>
   *   <li>Keycloak's {@code realm_access.roles} claim → also exposed as {@code SCOPE_*} authorities so the
   *       same security rules cover both client_credentials tokens (which carry scopes) and password-grant
   *       user tokens (which carry realm roles).</li>
   * </ul>
   */
  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
    scopeConverter.setAuthorityPrefix("SCOPE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
      Collection<GrantedAuthority> authorities = scopeConverter.convert(jwt);
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
        Collection<GrantedAuthority> roleAuthorities = roles.stream()
          .map(Object::toString)
          .map(role -> new SimpleGrantedAuthority("SCOPE_" + role))
          .collect(Collectors.toList());
        return Stream.concat(authorities.stream(), roleAuthorities.stream()).toList();
      }
      return authorities;
    });
    return converter;
  }
}
