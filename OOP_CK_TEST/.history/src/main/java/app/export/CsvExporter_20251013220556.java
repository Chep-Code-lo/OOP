package app.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.util.List;

public class CsvExporter {
    /**
     * Ghi dữ liệu ra file CSV với mã hóa UTF-8
     * @param fileName tên file
     * @param headers tiêu đề cột
     * @param rows dữ liệu từng dòng
     * @return đường dẫn file đã ghi
     * @throws Exception nếu có lỗi ghi file
     */
    public static Path writeCsv(String fileName, String[] headers, List<String[]> rows) throws Exception {
        Path path = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(String.join(",", headers));
            writer.newLine();
            for (String[] row : rows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
        return path;
    }
}
