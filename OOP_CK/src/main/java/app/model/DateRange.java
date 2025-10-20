package app.model;
import java.time.LocalDate;

/** Khoảng ngày đóng gói trong record. */
public record DateRange(LocalDate start, LocalDate end) {
    /** Đại diện khoảng toàn thời gian (MIN → MAX). */
    public static DateRange allTime() {
        return new DateRange(LocalDate.MIN, LocalDate.MAX);
    }
}
