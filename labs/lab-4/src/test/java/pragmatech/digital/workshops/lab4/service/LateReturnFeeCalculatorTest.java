package pragmatech.digital.workshops.lab4.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;
import pragmatech.digital.workshops.lab4.support.BookMother;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  THE "STRONG" TEST — Kills all PIT mutants                              ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║                                                                         ║
 * ║  Compare this with LateReturnFeeCalculatorWeakTest.                     ║
 * ║  Same class under test, same scenarios — but with EXACT value           ║
 * ║  assertions that verify the actual fee amounts.                         ║
 * ║                                                                         ║
 * ║  Key differences:                                                       ║
 * ║  1. Every test asserts the EXACT BigDecimal value (isEqualByComparingTo)║
 * ║  2. Boundary pairs test BOTH sides of each threshold:                   ║
 * ║     - Day 7 (tier one) vs Day 8 (tier two)                             ║
 * ║     - Day 14 (tier two) vs Day 15 (tier three)                         ║
 * ║  3. Every BookStatus variant is tested (not just BORROWED)              ║
 * ║                                                                         ║
 * ║  These boundary tests are exactly what PIT forces you to write.         ║
 * ║  Without them, PIT's "conditionals boundary" mutator would change       ║
 * ║  <= 7 to < 7 and your tests would still pass.                          ║
 * ║                                                                         ║
 * ║  Run PIT and confirm:                                                   ║
 * ║  ./mvnw test-compile org.pitest:pitest-maven:mutationCoverage          ║
 * ║  → All mutants should be killed (green in the HTML report)              ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
@DisplayName("LateReturnFeeCalculator — Strong assertions (all mutants killed)")
class LateReturnFeeCalculatorTest {

  // ── Fixed clock for deterministic tests ────────────────────────────────
  //
  // Production code: LocalDate.now(clock)
  // Test code:       Clock.fixed(...)
  //
  // This is the standard pattern for testing time-dependent logic in Spring.
  // The production Clock bean comes from WebClientConfig (@Bean Clock clock()).
  // In tests we bypass Spring entirely — just pass a fixed Clock to the constructor.
  private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);
  private static final Clock FIXED_CLOCK = Clock.fixed(
    TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(),
    ZoneId.systemDefault()
  );

  private LateReturnFeeCalculator cut;

  @BeforeEach
  void setUp() {
    cut = new LateReturnFeeCalculator(FIXED_CLOCK);
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  BOOK STATUS CHECKS
  //  PIT mutator: "Negate Conditionals" → changes != to ==
  //  If we only test BORROWED books, PIT can negate the status check
  //  and our tests wouldn't notice. Testing all statuses kills this mutant.
  // ═══════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("When the book is not borrowed")
  class WhenBookIsNotBorrowed {

    @Test
    @DisplayName("Should return zero fee for an AVAILABLE book")
    void shouldReturnZeroFeeWhenBookIsAvailable() {
      Book availableBook = BookMother.availableBook();

      BigDecimal fee = cut.calculateFee(availableBook, TODAY.minusDays(10));

      // Exact value assertion — PIT can't replace ZERO with anything else
      assertThat(fee).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should return zero fee for a RESERVED book")
    void shouldReturnZeroFeeWhenBookIsReserved() {
      Book reservedBook = BookMother.reservedBook();

      BigDecimal fee = cut.calculateFee(reservedBook, TODAY.minusDays(10));

      assertThat(fee).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should return zero fee for a MAINTENANCE book")
    void shouldReturnZeroFeeWhenBookIsInMaintenance() {
      Book maintenanceBook = BookMother.maintenanceBook();

      BigDecimal fee = cut.calculateFee(maintenanceBook, TODAY.minusDays(10));

      assertThat(fee).isEqualByComparingTo("0");
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  NOT YET OVERDUE
  //  PIT mutator: "Conditionals Boundary" → changes <= 0 to < 0
  //  Testing with exactly 0 days overdue kills this mutant.
  // ═══════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("When the book is not yet overdue")
  class WhenNotOverdue {

    @Test
    @DisplayName("Should return zero fee when borrowed today (0 days overdue)")
    void shouldReturnZeroFeeWhenBorrowedToday() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY);

      assertThat(fee).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should return zero fee when return date is in the future")
    void shouldReturnZeroFeeWhenReturnDateInFuture() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.plusDays(5));

      assertThat(fee).isEqualByComparingTo("0");
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  TIER ONE: 1-7 days overdue at $1.00/day
  //
  //  PIT mutator: "Conditionals Boundary" → changes <= 7 to < 7
  //  If we only test day 3, both <= 7 and < 7 would put it in tier one.
  //  Testing at EXACTLY day 7 kills this mutant:
  //    - Original: 7 <= 7 → true  → tier one ($7.00)
  //    - Mutated:  7 < 7  → false → falls to tier two ($10.50) → TEST FAILS
  //
  //  This is the KEY INSIGHT of mutation testing: boundary values matter!
  // ═══════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Tier one: 1-7 days overdue ($1.00/day)")
  class TierOne {

    @Test
    @DisplayName("Should charge $1.00 for 1 day overdue")
    void shouldChargeTierOneRateForOneDay() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(1));

      // 1 day x $1.00 = $1.00
      assertThat(fee).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("Should charge $7.00 for exactly 7 days (upper boundary)")
    void shouldChargeTierOneRateAtUpperBoundary() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(7));

      // 7 days x $1.00 = $7.00
      // This test KILLS the "conditional boundary" mutant (<= 7 → < 7)
      assertThat(fee).isEqualByComparingTo("7.00");
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  TIER TWO: 8-14 days overdue at $1.50/day
  //
  //  Same principle: test at day 8 (just crossed from tier one into two)
  //  and at day 14 (upper boundary of tier two).
  //
  //  PIT mutator: "Math" → changes multiply to divide
  //  Exact value assertions catch this because $1.50 * 8 = $12.00 but
  //  $1.50 / 8 = $0.1875 — the test would fail immediately.
  // ═══════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Tier two: 8-14 days overdue ($1.50/day)")
  class TierTwo {

    @Test
    @DisplayName("Should charge $12.00 for 8 days (just crossed into tier two)")
    void shouldChargeTierTwoRateJustAfterBoundary() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(8));

      // 8 days x $1.50 = $12.00
      // This test KILLS the boundary mutant (<= 7 → < 7) from the other side:
      //   - Original: 8 <= 7 → false → tier two ($12.00)
      //   - Mutated:  8 < 7  → false → tier two ($12.00) — same result here
      // But combined with the day-7 test above, the mutant IS killed.
      assertThat(fee).isEqualByComparingTo("12.00");
    }

    @Test
    @DisplayName("Should charge $21.00 for exactly 14 days (upper boundary)")
    void shouldChargeTierTwoRateAtUpperBoundary() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(14));

      // 14 days x $1.50 = $21.00
      // Kills the "conditional boundary" mutant (<= 14 → < 14)
      assertThat(fee).isEqualByComparingTo("21.00");
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  TIER THREE: 15+ days overdue at $2.00/day
  //
  //  PIT mutator: "Return Values" → replaces return value with default
  //  If we didn't test tier three, PIT could replace the return with
  //  BigDecimal.ZERO and no test would fail.
  // ═══════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Tier three: 15+ days overdue ($2.00/day)")
  class TierThree {

    @Test
    @DisplayName("Should charge $30.00 for 15 days (just crossed into tier three)")
    void shouldChargeTierThreeRateJustAfterBoundary() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(15));

      // 15 days x $2.00 = $30.00
      // Kills the boundary mutant (<= 14 → < 14) from the other side
      assertThat(fee).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("Should charge $60.00 for 30 days overdue")
    void shouldChargeTierThreeRateForThirtyDays() {
      Book borrowedBook = BookMother.borrowedBook();

      BigDecimal fee = cut.calculateFee(borrowedBook, TODAY.minusDays(30));

      // 30 days x $2.00 = $60.00
      assertThat(fee).isEqualByComparingTo("60.00");
    }
  }
}
