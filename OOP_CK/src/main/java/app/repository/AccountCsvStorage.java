package app.repository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;


public final class AccountCsvStorage {
    private static final Path FILE = Paths.get("data", "accounts.csv");
    private AccountCsvStorage() {}

    /** Ghi toàn bộ danh sách tài khoản ra file CSV để đồng bộ dữ liệu. */
    public static synchronized void persist(List<Map<String, Object>> accounts) {
        try {
            Files.createDirectories(FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(
                    FILE,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write("id,name,type,balance,note");
                writer.newLine();
                for (Map<String, Object> row : accounts) {
                    writer.write(String.join(",",
                            esc(row.get(DataStore.AccountFields.ID)),
                            esc(row.get(DataStore.AccountFields.NAME)),
                            esc(row.get(DataStore.AccountFields.TYPE)),
                            esc(row.get(DataStore.AccountFields.BALANCE)),
                            esc(row.get(DataStore.AccountFields.NOTE))
                    ));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Không thể cập nhật accounts.csv: " + e.getMessage());
        }
    }

    /** Escape giá trị để an toàn trong CSV. */
    private static String esc(Object value) {
        String s = (value == null) ? "" : value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
