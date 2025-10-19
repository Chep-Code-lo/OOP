package app.export;
import java.nio.file.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvExporter {

    /**
     * Ghi dữ liệu ra file CSV với UTF-8 + BOM (hiển thị tiếng Việt chuẩn trong Excel)
     * và tự lưu vào thư mục "data" nếu chưa có.
     *
     * @param fileName tên file CSV (ví dụ: "transactions.csv")
     * @param headers tiêu đề cột
     * @param rows dữ liệu từng dòng
     * @return đường dẫn file đã ghi
     * @throws Exception nếu có lỗi ghi file
     */
    public static Path writeCsv(String fileName, String[] headers, List<String[]> rows) throws Exception {
        Path dir = Paths.get("data");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path path = dir.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write(String.join(",", headers));
            writer.newLine();
            for (String[] row : rows) { // mỗi phần tử đã được chuẩn hóa thành chuỗi ở exporter.
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
        System.out.println("Đã lưu CSV tại: " + path.toAbsolutePath());
        return path;
    }
}
