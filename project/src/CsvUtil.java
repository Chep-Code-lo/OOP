import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
public class CsvUtil {
     private static final Logger log = LoggerSetup.get(CsvUtil.class);
    //Đọc danh sách từ file csv
    public static List<Book> readBooks(String path) throws IOException{
        log.info("Reading CSV: " + Paths.get(path).toAbsolutePath());
        List<Book> books = new ArrayList<>();
        Path p = Paths.get(path);
        if(!Files.exists(p)){
            log.warning("CSV not found: " + p.toAbsolutePath());
            return books;
        } 
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        boolean headerskipped = false;

        for(String line : lines){
            if(!headerskipped){
                headerskipped = true;
                continue;
            }
            String[] parts = line.split(",");
            if(parts.length < 6) continue;
            Book b = new Book(parts[0], parts[1], parts[2], parts[3],Integer.parseInt(parts[4]), parts[5]);
            books.add(b);
        }
        log.info("Parsed " + books.size() + " rows");
        return books;
    }

    //Ghi danh sách book ra file csv
    public static void writeBooks(String path, List<Book>books) throws IOException{
        log.info(() -> "Writing " + books.size() + " books to " + Paths.get(path).toAbsolutePath());
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),StandardCharsets.UTF_8)){
            writer.write("id, tên, tác giả, thể loại, số lượng, trạng thái\n");
            for(Book b : books){
                writer.write(b.toString() + "\n");
            }
        }
        log.info("Done writing CSV");
    }

}
