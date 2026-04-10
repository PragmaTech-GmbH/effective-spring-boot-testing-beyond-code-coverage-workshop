package pragmatech.digital.workshops.lab4.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;

@Service
public class LateReturnFeeCalculator {

  private static final BigDecimal RATE_TIER_ONE = new BigDecimal("1.00");
  private static final BigDecimal RATE_TIER_TWO = new BigDecimal("1.50");
  private static final BigDecimal RATE_TIER_THREE = new BigDecimal("2.00");

  private final Clock clock;

  public LateReturnFeeCalculator(Clock clock) {
    this.clock = clock;
  }

  public BigDecimal calculateFee(Book book, LocalDate borrowedDate) {
    if (book.getStatus() != BookStatus.BORROWED) {
      return BigDecimal.ZERO;
    }

    LocalDate today = LocalDate.now(clock);
    long daysOverdue = ChronoUnit.DAYS.between(borrowedDate, today);

    if (daysOverdue <= 0) {
      return BigDecimal.ZERO;
    } else if (daysOverdue <= 7) {
      return RATE_TIER_ONE.multiply(BigDecimal.valueOf(daysOverdue));
    } else if (daysOverdue <= 14) {
      return RATE_TIER_TWO.multiply(BigDecimal.valueOf(daysOverdue));
    } else {
      return RATE_TIER_THREE.multiply(BigDecimal.valueOf(daysOverdue));
    }
  }
}
