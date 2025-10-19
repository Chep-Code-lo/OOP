package app.payment;

import java.time.LocalDate;

public record DateRange(LocalDate start, LocalDate end) {
    public static DateRange allTime() {
        return new DateRange(LocalDate.MIN, LocalDate.MAX);
    }
}
