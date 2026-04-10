package pragmatech.digital.workshops.lab4.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.support.BookMother;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  THE "WEAK" TEST — Demonstrates why line coverage lies                  ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║                                                                         ║
 * ║  This test class achieves 100% LINE COVERAGE on LateReturnFeeCalculator ║
 * ║  according to JaCoCo. Every branch is entered, every line is executed.  ║
 * ║                                                                         ║
 * ║  BUT — the assertions are deliberately weak:                            ║
 * ║  - isNotNull() accepts ANY non-null value                               ║
 * ║  - isInstanceOf(BigDecimal.class) is always true for BigDecimal         ║
 * ║  - isPositive() doesn't verify the actual amount                        ║
 * ║                                                                         ║
 * ║  When you run PIT against this test, most mutants will SURVIVE:         ║
 * ║  - Changing <= 7 to < 7 won't break any test                           ║
 * ║  - Swapping RATE_TIER_TWO with RATE_TIER_ONE won't break any test      ║
 * ║  - Returning a different BigDecimal value won't break any test          ║
 * ║                                                                         ║
 * ║  Try it yourself:                                                       ║
 * ║  1. ./mvnw test-compile org.pitest:pitest-maven:mutationCoverage       ║
 * ║  2. open target/pit-reports/index.html                                  ║
 * ║  3. Look at the red lines (survived mutants) — those are the gaps!     ║
 * ║                                                                         ║
 * ║  Then switch to LateReturnFeeCalculatorTest to see how strong           ║
 * ║  assertions kill all those same mutants.                                ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
@DisplayName("LateReturnFeeCalculator — Weak assertions (mutation testing demo)")
class LateReturnFeeCalculatorWeakTest {

  // ── Step 1: Fix the clock so tests are deterministic ──────────────────
  // We pin "today" to 2026-01-15 so that calculating days overdue is predictable.
  // The production code uses Clock injection (not LocalDate.now() directly),
  // which makes this possible without any Spring context.
  private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);
  private static final Clock FIXED_CLOCK = Clock.fixed(
    TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(),
    ZoneId.systemDefault()
  );

  // ── Step 2: Create the class under test (plain Java, no Spring needed) ─
  // PIT works best with fast unit tests. No @SpringBootTest, no containers,
  // no application context. Just new the class and inject a fixed Clock.
  private LateReturnFeeCalculator cut;

  @BeforeEach
  void setUp() {
    cut = new LateReturnFeeCalculator(FIXED_CLOCK);
  }

  // ── Step 3: Write tests that cover all branches but assert weakly ──────

  @Test
  @DisplayName("Returns a fee when book is overdue by 10 days (tier two)")
  void shouldReturnFeeWhenBookIsOverdueTierTwo() {
    // Arrange: borrowed book, 10 days overdue → lands in tier two ($1.50/day)
    Book borrowedBook = BookMother.borrowedBook();
    LocalDate borrowedDate = TODAY.minusDays(10);

    // Act
    BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

    // Assert — WEAK: we only check it's not null, not the actual value!
    // PIT will mutate the multiplication factor or tier boundaries and
    // this test will still pass because we never check the exact amount.
    assertThat(fee).isNotNull();
    assertThat(fee).isInstanceOf(BigDecimal.class);
  }

  @Test
  @DisplayName("Returns a fee when book is overdue by 3 days (tier one)")
  void shouldReturnFeeWhenBookIsOverdueTierOne() {
    Book borrowedBook = BookMother.borrowedBook();
    LocalDate borrowedDate = TODAY.minusDays(3);

    BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

    // WEAK: isPositive() doesn't tell us if the amount is $3.00 or $300.00
    assertThat(fee).isPositive();
  }

  @Test
  @DisplayName("Returns a fee when book is overdue by 20 days (tier three)")
  void shouldReturnFeeWhenBookIsOverdueTierThree() {
    Book borrowedBook = BookMother.borrowedBook();
    LocalDate borrowedDate = TODAY.minusDays(20);

    BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

    // WEAK: same problem — any positive number passes
    assertThat(fee).isPositive();
  }

  @Test
  @DisplayName("Returns zero when book is not borrowed")
  void shouldReturnZeroWhenBookIsNotBorrowed() {
    Book availableBook = BookMother.availableBook();
    LocalDate borrowedDate = TODAY.minusDays(10);

    BigDecimal fee = cut.calculateFee(availableBook, borrowedDate);

    // This one is actually okay — but it's not enough on its own
    assertThat(fee).isNotNull();
  }

  @Test
  @DisplayName("Returns zero when book is not yet overdue")
  void shouldReturnZeroWhenNotOverdue() {
    Book borrowedBook = BookMother.borrowedBook();
    LocalDate borrowedDate = TODAY; // borrowed today, 0 days overdue

    BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

    assertThat(fee).isNotNull();
  }

  // ──────────────────────────────────────────────────────────────────────
  // After running PIT you'll see something like:
  //
  //   >> Generated 12 mutations Killed 4 (33%)
  //   >> Mutations are detailed in target/pit-reports/index.html
  //
  // The surviving mutants tell you EXACTLY what your tests are missing:
  //
  //   SURVIVED: changed conditional boundary (daysOverdue <= 7 → < 7)
  //   SURVIVED: replaced multiply return value (RATE_TIER_TWO → RATE_TIER_ONE)
  //   SURVIVED: replaced BigDecimal return with BigDecimal.ZERO
  //
  // These are real bugs that your "100% coverage" tests would miss!
  // See LateReturnFeeCalculatorTest for the fix.
  // ──────────────────────────────────────────────────────────────────────
}
