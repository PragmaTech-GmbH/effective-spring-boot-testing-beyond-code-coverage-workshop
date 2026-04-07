package pragmatech.digital.workshops.lab3.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Order(1)
@Component
public class BlockBotRequestFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(BlockBotRequestFilter.class);

  private static final List<String> BLOCKED_USER_AGENT_KEYWORDS = Arrays.asList(
    "openai", "claude", "anthropic", "gpt"
  );

  @Override
  public void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {

    String userAgent = Optional
      .ofNullable(request.getHeader("User-Agent"))
      .orElse("").toLowerCase();

    boolean isLlmBot = BLOCKED_USER_AGENT_KEYWORDS.stream()
      .anyMatch(userAgent::contains);

    if (isLlmBot) {
      logger.warn("Blocked request from LLM bot with User-Agent: {}", userAgent);

      response.setStatus(HttpStatus.I_AM_A_TEAPOT.value());
      response.getWriter().write("{}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
