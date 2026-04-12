package pragmatech.digital.workshops.lab4.experiment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.service.LateReturnFeeCalculator;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LateReturnFeeCalculator — Weak assertions (mutation testing demo)")
class LateReturnFeeCalculatorWeakTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);
  private static final Clock FIXED_CLOCK = Clock.fixed(
    TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(),
    ZoneId.systemDefault()
  );

  private LateReturnFeeCalculator cut = new LateReturnFeeCalculator(FIXED_CLOCK);


  @Test
  @DisplayName("Returns a fee when book is overdue by 10 days (tier two)")
  void shouldReturnFeeWhenBookIsOverdueTierTwo() {
    Book borrowedBook = BookMother.borrowedBook();
    LocalDate borrowedDate = TODAY.minusDays(10);

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
}
