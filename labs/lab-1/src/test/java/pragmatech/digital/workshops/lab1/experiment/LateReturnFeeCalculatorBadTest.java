package pragmatech.digital.workshops.lab1.experiment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pragmatech.digital.workshops.lab1.entity.Book;
import pragmatech.digital.workshops.lab1.entity.BookStatus;
import pragmatech.digital.workshops.lab1.service.LateReturnFeeCalculator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Intentionally bad test suite for {@link LateReturnFeeCalculator}.
 *
 * Line coverage: 100% — every branch is executed.
 * Mutation coverage: very low — assertions are too weak to detect
 * when PIT changes boundary conditions or rate constants.
 *
 * Surviving mutations include:
 * - Replacing {@code return BigDecimal.ZERO} with any non-null value  (isNotNull passes)
 * - Changing {@code <= 7} to {@code < 7} (mid-tier day 4 stays in tier 1)
 * - Changing {@code <= 14} to {@code < 14} (mid-tier day 11 stays in tier 2)
 * - Swapping RATE_TIER_ONE for RATE_TIER_TWO (isPositive still passes)
 */
class LateReturnFeeCalculatorBadTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(
    Instant.parse("2025-06-01T00:00:00Z"),
    ZoneOffset.UTC
  );

  private LateReturnFeeCalculator cut;
  private Book borrowedBook;

  @BeforeEach
  void setUp() {
    cut = new LateReturnFeeCalculator(FIXED_CLOCK);
    borrowedBook = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
    borrowedBook.setStatus(BookStatus.BORROWED);
  }

  @Test
  void shouldReturnSomethingWhenBookIsNotBorrowed() {
    Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
    book.setStatus(BookStatus.AVAILABLE);

    BigDecimal fee = cut.calculateFee(book, LocalDate.of(2025, 5, 1));

    // covers the branch but isNotNull() passes even when PIT replaces
    // return BigDecimal.ZERO with return RATE_TIER_ONE.multiply(BigDecimal.valueOf(31))
    assertThat(fee).isNotNull();
  }

  @Test
  void shouldReturnSomethingWhenBookIsReturnedOnTime() {
    BigDecimal fee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 6, 1));

    // covers daysOverdue <= 0 branch; isNotNull() survives any non-null return mutation
    assertThat(fee).isNotNull();
  }

  @Test
  void shouldReturnPositiveFeeWhenBookIsOverdue() {
    // Day 4 — mid-tier 1; day 11 — mid-tier 2; day 20 — mid-tier 3.
    // All three overdue branches are executed → 100% line coverage.
    // But mid-tier inputs mean boundary mutations (<= 7 → < 7, <= 14 → < 14) go undetected,
    // and isPositive() cannot distinguish the wrong rate being applied.
    BigDecimal tierOneFee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 5, 28));   // 4 days
    BigDecimal tierTwoFee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 5, 21));   // 11 days
    BigDecimal tierThreeFee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 5, 12)); // 20 days

    assertThat(tierOneFee).isPositive();
    assertThat(tierTwoFee).isPositive();
    assertThat(tierThreeFee).isPositive();
  }
}
