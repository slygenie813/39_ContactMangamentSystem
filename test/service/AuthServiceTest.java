package service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistance.ActivityLog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @TempDir
    Path dir;

    @BeforeAll
    static void sendLogToTempFile() {
        ActivityLog.setFile(Paths.get(System.getProperty("java.io.tmpdir"), "cms-test.log"));
    }

    private AuthService service() {
        return new AuthService(dir.resolve("users.txt"));
    }

    @Test
    void registerThenLoginSucceeds() throws Exception {
        AuthService auth = service();
        auth.register("alice", "alice@example.com", "secret123".toCharArray());
        assertTrue(auth.login("alice", "secret123".toCharArray()));
    }

    @Test
    void wrongPasswordOrUnknownUserFails() throws Exception {
        AuthService auth = service();
        auth.register("alice", "alice@example.com", "secret123".toCharArray());
        assertFalse(auth.login("alice", "wrong-pass".toCharArray()));
        assertFalse(auth.login("nobody", "secret123".toCharArray()));
        assertFalse(auth.login("alice", null));
    }

    @Test
    void usernameIsCaseInsensitive() throws Exception {
        AuthService auth = service();
        auth.register("Alice", "alice@example.com", "secret123".toCharArray());
        assertTrue(auth.login("ALICE", "secret123".toCharArray()));
        assertThrows(AuthException.class,
                () -> auth.register("aLiCe", "other@example.com", "secret123".toCharArray()));
    }

    @Test
    void rejectsWeakOrInvalidRegistrations() {
        AuthService auth = service();
        assertThrows(AuthException.class,
                () -> auth.register("alice", "alice@example.com", "12345".toCharArray()));  // short pw
        assertThrows(AuthException.class,
                () -> auth.register("ab", "alice@example.com", "secret123".toCharArray())); // short user
        assertThrows(AuthException.class,
                () -> auth.register("a b c", "alice@example.com", "secret123".toCharArray()));
        assertThrows(AuthException.class,
                () -> auth.register("alice", "not-an-email", "secret123".toCharArray()));
        assertThrows(AuthException.class,
                () -> auth.register("alice", "alice@example.com", null));
    }

    @Test
    void passwordArrayIsClearedAfterUse() throws Exception {
        char[] pw = "secret123".toCharArray();
        service().register("alice", "alice@example.com", pw);
        assertArrayEquals(new char[pw.length], pw);
    }

    @Test
    void passwordIsNeverStoredInPlainText() throws Exception {
        service().register("alice", "alice@example.com", "secret123".toCharArray());
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("secret123"));
        assertTrue(content.startsWith("alice:alice@example.com:"));
    }

    @Test
    void samePasswordGivesDifferentStoredHashes() throws Exception {
        AuthService auth = service();
        auth.register("alice", "alice@example.com", "secret123".toCharArray());
        auth.register("bobby", "bobby@example.com", "secret123".toCharArray());

        String[] lines = Files.readString(dir.resolve("users.txt")).split("\\R");
        String[] a = lines[0].split(":");
        String[] b = lines[1].split(":");
        assertNotEquals(a[3], b[3]);   // different salts
        assertNotEquals(a[4], b[4]);   // therefore different hashes
    }

    @Test
    void legacyPlainTextAccountsAreMigratedToHashes() throws Exception {
        Files.writeString(dir.resolve("users.txt"), "bob,hunter22\n");
        AuthService auth = service();

        assertTrue(auth.login("bob", "hunter22".toCharArray()));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("hunter22"));
        assertTrue(auth.login("bob", "hunter22".toCharArray()));   // still works after migration
    }
}