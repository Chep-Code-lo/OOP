package app.report;

import app.model.DateRange;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** mô tả text cho tham số lọc báo cáo. */
final class ReportUtils {
    private ReportUtils() {}

    /** Diễn giải khoảng ngày thành chuỗi dễ đọc. */
    static String describeRange(DateRange range) {
        if (range == null) return "Không giới hạn";
        LocalDate start = range.start();
        LocalDate end = range.end();
        boolean startMin = LocalDate.MIN.equals(start);
        boolean endMax = LocalDate.MAX.equals(end);
        if (startMin && endMax) return "Toàn thời gian";
        if (startMin) return "Đến " + end;
        if (endMax) return "Từ " + start;
        if (start.equals(end)) return start.toString();
        return start + " - " + end;
    }

    /** Ghép danh sách lựa chọn thành chuỗi (fallback khi rỗng). */
    static String describeSelection(List<String> selections, String fallback) {
        if (selections == null || selections.isEmpty()) {
            return fallback;
        }
        return selections.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("; "));
    }
}
