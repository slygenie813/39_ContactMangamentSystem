package persistance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityLogTest {

    @TempDir
    Path dir;

    private Path original;

    @BeforeEach
    void redirectLog() {
        original = ActivityLog.getFile();
        ActivityLog.setFile(dir.resolve("test.log"));
    }

    @AfterEach
    void restoreLog() {
        ActivityLog.setFile(original);
    }

    @Test
    void writesOneLinePerEntryInOrder() throws IOException {
        ActivityLog.log("ADD", "Jane Doe");
        ActivityLog.log("DELETE", "Jane Doe");

        List<String> lines = Files.readAllLines(dir.resolve("test.log"));
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains(" | ADD | Jane Doe"));
        assertTrue(lines.get(1).contains(" | DELETE | Jane Doe"));
    }

    @Test
    void newlinesInDetailCannotForgeExtraLines() throws IOException {
        ActivityLog.log("EDIT", "a\nFAKE | ADD | x\r\nb");

        List<String> lines = Files.readAllLines(dir.resolve("test.log"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("a FAKE | ADD | x b"));
    }

    @Test
    void nullDetailDoesNotThrow() {
        assertDoesNotThrow(() -> ActivityLog.log("ADD", null));
    }

    @Test
    void unwritableLogLocationNeverThrows() {
        ActivityLog.setFile(dir.resolve("no-such-folder").resolve("x.log"));
        assertDoesNotThrow(() -> ActivityLog.log("ADD", "should be swallowed"));
    }
}