import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.logging.*;

public class Main{
    private static final Logger log = LoggerSetup.get(Main.class);

    public static void main(String[] args) throws IOException{
        // Bật logger (console + logs/app.log nếu bạn dùng LoggerSetup như trước)
        LoggerSetup.init();

        String filePath = "data/books.csv";
        log.info("CWD  = " + Paths.get("").toAbsolutePath());
        log.info("CSV  = " + Paths.get(filePath).toAbsolutePath());

        List<Book> loaded = CsvUtil.readBooks(filePath);
        log.info("Loaded " + loaded.size() + " books");

        for(Book b : loaded){
            System.out.println(b);
        }
    }
}
