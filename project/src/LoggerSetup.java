import java.io.IOException;
import java.nio.file.*;
import java.util.logging.*;
import java.time.*;

public final class LoggerSetup {
    private static boolean inited = false;

    // Formatter ngắn gọn: 2025-09-06 20:10:30 INFO [CsvUtil] Reading CSV...
    static class Pretty extends Formatter {
        @Override public synchronized String format(LogRecord r) {
            LocalDateTime ts = LocalDateTime.ofInstant(r.getInstant(), java.time.ZoneId.systemDefault());
            return String.format("%s %-5s [%s] %s%n",
                    ts.toString().replace('T',' '),
                    r.getLevel().getName(),
                    r.getLoggerName(),
                    formatMessage(r));
        }
    }

    /** Gọi 1 lần ở đầu chương trình */
    public static synchronized void init() throws IOException {
        if (inited) return;
        inited = true;

        Logger root = Logger.getLogger("");
        // bỏ handler mặc định
        for (Handler h : root.getHandlers()) root.removeHandler(h);
        root.setLevel(Level.INFO);

        // Console
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(new Pretty());
        root.addHandler(ch);

        // File: logs/app.log (rolling 1MB x 3)
        Files.createDirectories(Paths.get("logs"));
        FileHandler fh = new FileHandler("logs/app.log", 1024*1024, 3, true);
        fh.setLevel(Level.INFO);
        fh.setFormatter(new Pretty());
        root.addHandler(fh);
    }

    /** Lấy logger cho lớp */
    public static Logger get(Class<?> c) {
        return Logger.getLogger(c.getName());
    }
}
