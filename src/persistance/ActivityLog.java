package persistance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Appends one line per action to a log file, for example:
 *
 *   2026-11-04 10:15:32 | ADD | Thabo Nkosi (0712345678)
 *   2026-11-04 10:16:01 | ADD_REJECTED | A contact with this phone ...
 *
 * Used for every add, edit and delete (and every rejected attempt), and for
 * login events. Passwords are never written here.
 *
 * Logging must never break the program: if the log file cannot be written
 * the problem is reported once on System.err and the program carries on.
 *
 * The default file is "contacts.log" in the working directory; set the
 * system property "contacts.log" to change it.
 */
public final class ActivityLog {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Path file =
            Paths.get(System.getProperty("contacts.log", "contacts.log"));

    private static boolean warned = false;

    private ActivityLog() {
        // static utility class
    }

    /** Changes the log file (mainly for tests). */
    public static synchronized void setFile(Path newFile) {
        file = newFile;
        warned = false;
    }

    public static synchronized Path getFile() {
        return file;
    }

    /** Writes one line: time | action | detail. Never throws. */
    public static synchronized void log(String action, String detail) {
        String line = LocalDateTime.now().format(TIME)
                + " | " + clean(action)
                + " | " + clean(detail) + System.lineSeparator();

        try {
            Files.write(file.toAbsolutePath(),
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException e) {
            if (!warned) {
                warned = true;
                System.err.println("Activity log could not be written to "
                        + file.toAbsolutePath() + ": " + e.getMessage());
            }
        }
    }

    // keep every entry on one line so the log cannot be forged or broken
    private static String clean(String text) {
        return text == null ? "" : text.replaceAll("[\\r\\n]+", " ").trim();
    }
}