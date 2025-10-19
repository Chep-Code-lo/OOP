import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

public class ContractStorage {
    private static final Path SAVE_PATH    = Paths.get("data", "contracts.csv");
    private static final Path COUNTER_PATH = Paths.get("data", "id_counters.properties");
    private static final int  ID_WIDTH     = 4; // -> Co0001, Ch0001 (đổi 5 nếu muốn Co00001)
    public static void saveBorrow(String status,
                                  String name,
                                  double money,
                                  String phone,
                                  String dueDate,
                                  double interest,
                                  String note) throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());

        boolean existed = Files.exists(SAVE_PATH);
        String id = nextIdForStatus(status); //

        try (BufferedWriter w = Files.newBufferedWriter(
                SAVE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            if (!existed) {
                w.write("id,status,name,money,phone,dueDate,interest,note,createdAt");
                w.newLine();
            }

            String createdAt = LocalDateTime.now().toString();

            String line = String.join(",",
                    esc(id), esc(status), esc(name), String.valueOf(money) + " VND", esc(phone),
                    esc(dueDate), String.valueOf(interest), esc(note), esc(createdAt)
            );
            w.write(line);
            w.newLine();
        }
    }

    // ====== Sinh ID tuần tự theo stats ======
    private static synchronized String nextIdForStatus(String status) throws IOException {
        String prefix = statusPrefix(status); // CoNo -> "Co", ChNo -> "Ch"

        Properties p = new Properties();
        if (Files.exists(COUNTER_PATH)) {
            try (var in = Files.newInputStream(COUNTER_PATH)) { p.load(in); }
        }
        int last = Integer.parseInt(p.getProperty(prefix, "0"));
        int next = last + 1;

        p.setProperty(prefix, Integer.toString(next));
        Files.createDirectories(COUNTER_PATH.getParent());
        try (var out = Files.newOutputStream(COUNTER_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            p.store(out, "ID counters per prefix (Co/Ch)");
        }

        return prefix + String.format("%0" + ID_WIDTH + "d", next);
    }

    private static String statusPrefix(String status) {
        if (status == null) return "Co";
        switch (status) {
            case "CoNo": return "Co";
            case "ChNo": return "Ch";
            default:     return status.substring(0, Math.min(2, status.length()));
        }
    }

    private static String esc(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
