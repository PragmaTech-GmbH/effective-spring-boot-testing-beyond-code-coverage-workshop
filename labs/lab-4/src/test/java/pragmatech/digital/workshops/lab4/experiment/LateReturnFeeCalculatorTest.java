package pragmatech.digital.workshops.lab4.experiment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;
import pragmatech.digital.workshops.lab4.service.LateReturnFeeCalculator;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class LateReturnFeeCalculatorTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(
    Instant.parse("2025-06-01T00:00:00Z"),
    ZoneOffset.UTC
  );

  private LateReturnFeeCalculator cut;

  @BeforeEach
  void setUp() {
    cut = new LateReturnFeeCalculator(FIXED_CLOCK);
  }

  @Nested
  class WhenBookIsNotBorrowed {

    @Test
    void shouldReturnZeroFeeWhenBookIsAvailable() {
      Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      book.setStatus(BookStatus.AVAILABLE);

      BigDecimal fee = cut.calculateFee(book, LocalDate.of(2025, 5, 1));

      assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroFeeWhenBookIsReserved() {
      Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      book.setStatus(BookStatus.RESERVED);

      BigDecimal fee = cut.calculateFee(book, LocalDate.of(2025, 5, 1));

      assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }
  }

  @Nested
  class WhenBookIsBorrowed {

    private Book borrowedBook;

    @BeforeEach
    void setUp() {
      borrowedBook = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      borrowedBook.setStatus(BookStatus.BORROWED);
    }

    @Test
    void shouldReturnZeroFeeWhenReturnedOnTime() {
      BigDecimal fee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 6, 1));

      assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroFeeWhenBorrowedInFuture() {
      BigDecimal fee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 6, 15));

      assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "1,  1.00",
      "3,  3.00",
      "7,  7.00"
    })
    void shouldChargeOneDollarPerDayWhenOneToSevenDaysOverdue(long daysOverdue, BigDecimal expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualByComparingTo(expectedFee);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "8,  12.00",
      "10, 15.00",
      "14, 21.00"
    })
    void shouldChargeDollarFiftyPerDayWhenEightToFourteenDaysOverdue(long daysOverdue, BigDecimal expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualByComparingTo(expectedFee);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "15, 30.00",
      "20, 40.00",
      "30, 60.00"
    })
    void shouldChargeTwoDollarsPerDayWhenFifteenOrMoreDaysOverdue(long daysOverdue, BigDecimal expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualByComparingTo(expectedFee);
    }

    @Nested
    class BoundaryValues {

      @Test
      void shouldApplyTierOneBoundaryAtSevenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 25);

        BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualByComparingTo(new BigDecimal("7.00"));
      }

      @Test
      void shouldApplyTierTwoBoundaryAtEightDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 24);

        BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualByComparingTo(new BigDecimal("12.00"));
      }

      @Test
      void shouldApplyTierTwoBoundaryAtFourteenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 18);

        BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualByComparingTo(new BigDecimal("21.00"));
      }

      @Test
      void shouldApplyTierThreeBoundaryAtFifteenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 17);

        BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualByComparingTo(new BigDecimal("30.00"));
      }
    }
  }
}
