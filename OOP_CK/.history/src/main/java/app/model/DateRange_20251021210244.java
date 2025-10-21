package app.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public record DateRange(LocalDate start, LocalDate end) {

    public static DateRange allTime() {
        return new DateRange(LocalDate.MIN, LocalDate.MAX);
    }

    public enum Granularity { DAY, WEEK, MONTH, YEAR }

    public static LocalDate bucketStart(LocalDate d, Granularity g) {
        return switch (g) {
            case DAY   -> d;
            case WEEK  -> d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> d.withDayOfMonth(1);
            case YEAR  -> d.withDayOfYear(1);
        };
    }

    public static String bucketKey(LocalDate d, Granularity g) {
        return switch (g) {
            case DAY   -> d.toString();                                        // 2025-10-21
            case WEEK  -> bucketStart(d, g).toString();                         // 2025-10-20 (đầu tuần)
            case MONTH -> d.getYear() + "-" + String.format("%02d", d.getMonthValue()); // 2025-10
            case YEAR  -> String.valueOf(d.getYear());                          // 2025
        };
    }
}
