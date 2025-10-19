package app.payment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ghi nhận thao tác người dùng trong phần báo cáo/payment xuống file log.
 */
public final class ActionLogger {
    private static final Path LOG_DIR = Paths.get("data");
    private static final Path LOG_FILE = LOG_DIR.resolve("payment_actions.csv");
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String HEADER = "timestamp,action";

    private ActionLogger() {}

    /**
     * Thêm một dòng log, tự động tạo thư mục/file nếu chưa tồn tại.
     */
    public static void logAction(String actionDescription) {
        String timestamp = TS_FORMAT.format(LocalDateTime.now());
        try {
            Files.createDirectories(LOG_DIR);
            ensureHeader();
            String line = csvEscape(timestamp) + "," + csvEscape(actionDescription) + System.lineSeparator();
            Files.writeString(LOG_FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Không thể ghi log thao tác payment (CSV): " + e.getMessage());
        }
    }

    private static void ensureHeader() throws IOException {
        boolean needHeader = Files.notExists(LOG_FILE);
        if (!needHeader) {
            try {
                needHeader = Files.size(LOG_FILE) == 0;
            } catch (IOException ignored) {
                needHeader = true;
            }
        }
        if (needHeader) {
            Files.writeString(LOG_FILE, HEADER + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private static String csvEscape(String value) {
        String v = value == null ? "" : value;
        String escaped = v.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
