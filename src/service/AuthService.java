/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import persistance.ActivityLog;
import persistance.FileHandler;
import persistance.PersistenceException;
import util.Validation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Creates accounts and checks logins. Passwords are never stored: each
 * account keeps a random salt and a PBKDF2-HMAC-SHA256 hash of the password.
 *
 * File format (users.txt, one account per line, fields separated by ':'):
 *
 *   username:email:iterations:salt:hash        (salt and hash are Base64)
 *
 * Older files whose lines look like "username,password" (plain text) are
 * converted to the hashed format automatically the first time the file is
 * read, so existing accounts keep working.
 *
 * Rules: username 3-30 letters, digits, '_', '.' or '-' (not case
 * sensitive, must be unique); password at least 6 characters; valid email.
 *
 * The default file is "users.txt" in the working directory; set the system
 * property "users.file" to change it.
 */
public class AuthService {

    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");

    private static final SecureRandom RANDOM = new SecureRandom();

    private static AuthService instance;

    private final Path file;

    /** One parsed line of the accounts file. */
    private static final class Entry {
        final String raw;       // the line as stored (kept for unknown lines)
        final String username;  // null when the line is not an account
        final String email;
        final int iterations;
        final byte[] salt;
        final byte[] hash;

        Entry(String raw, String username, String email,
              int iterations, byte[] salt, byte[] hash) {
            this.raw = raw;
            this.username = username;
            this.email = email;
            this.iterations = iterations;
            this.salt = salt;
            this.hash = hash;
        }

        String toLine() {
            return username + ":" + email + ":" + iterations + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        }
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService(
                    Paths.get(System.getProperty("users.file", "users.txt")));
        }
        return instance;
    }

    public AuthService(Path file) {
        this.file = file;
    }

    // --------------------------------------------------
    // Registering
    // --------------------------------------------------

    /**
     * Creates an account. The password array is cleared afterwards.
     *
     * @throws AuthException        if the username, email or password is
     *                              not acceptable, or the username is taken
     * @throws PersistenceException if the accounts file cannot be read/saved
     */
    public synchronized void register(
            String username, String email, char[] password)
            throws AuthException {

        try {
            String name = username == null ? "" : username.trim();
            String mail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

            if (!USERNAME_PATTERN.matcher(name).matches()) {
                reject(name, "Username must be 3-30 characters: letters, "
                        + "digits, underscore, dot or hyphen.");
            }
            if (!Validation.isValidEmail(mail)) {
                reject(name, "Please enter a valid email address.");
            }
            if (password == null || password.length < MIN_PASSWORD_LENGTH) {
                reject(name, "Password must be at least "
                        + MIN_PASSWORD_LENGTH + " characters.");
            }

            List<Entry> entries = loadEntries();
            for (Entry e : entries) {
                if (e.username != null && e.username.equalsIgnoreCase(name)) {
                    reject(name, "That username is already taken.");
                }
            }

            byte[] salt = new byte[SALT_BYTES];
            RANDOM.nextBytes(salt);
            byte[] hash = hash(password, salt, ITERATIONS);

            entries.add(new Entry(null, name, mail, ITERATIONS, salt, hash));
            saveEntries(entries);
            ActivityLog.log("REGISTER", name);

        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private void reject(String username, String message) throws AuthException {
        ActivityLog.log("REGISTER_REJECTED", message + " [" + username + "]");
        throw new AuthException(message);
    }

    // --------------------------------------------------
    // Logging in
    // --------------------------------------------------

    /**
     * Checks a username and password. The password array is cleared
     * afterwards.
     *
     * @throws PersistenceException if the accounts file cannot be read
     */
    public synchronized boolean login(String username, char[] password) {
        try {
            String name = username == null ? "" : username.trim();
            Entry found = null;

            for (Entry e : loadEntries()) {
                if (e.username != null && e.username.equalsIgnoreCase(name)) {
                    found = e;
                    break;
                }
            }

            boolean ok;
            if (found == null || password == null) {
                // do the same amount of work, so timing does not reveal
                // whether the username exists
                byte[] dummySalt = new byte[SALT_BYTES];
                hash(password == null ? new char[0] : password,
                        dummySalt, ITERATIONS);
                ok = false;
            } else {
                byte[] attempt = hash(password, found.salt, found.iterations);
                ok = MessageDigest.isEqual(attempt, found.hash);
            }

            ActivityLog.log(ok ? "LOGIN_OK" : "LOGIN_FAILED", name);
            return ok;

        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    // --------------------------------------------------
    // File handling
    // --------------------------------------------------

    /**
     * Reads the file. Old "username,password" lines are converted to hashed
     * accounts and the file is rewritten once.
     */
    private List<Entry> loadEntries() {
        List<String> lines = FileHandler.readLines(file);
        List<Entry> entries = new ArrayList<>();
        boolean migrated = false;

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            Entry parsed = parseHashedLine(line);
            if (parsed != null) {
                entries.add(parsed);
                continue;
            }

            Entry legacy = migrateLegacyLine(line);
            if (legacy != null) {
                entries.add(legacy);
                migrated = true;
            } else {
                // not an account we understand: keep the line untouched
                entries.add(new Entry(line, null, null, 0, null, null));
            }
        }

        if (migrated) {
            saveEntries(entries);
            ActivityLog.log("USERS_MIGRATED",
                    "plain-text accounts converted to hashed accounts");
        }
        return entries;
    }

    private void saveEntries(List<Entry> entries) {
        List<String> lines = new ArrayList<>();
        for (Entry e : entries) {
            lines.add(e.username == null ? e.raw : e.toLine());
        }
        FileHandler.writeLinesAtomically(file, lines);
    }

    private static Entry parseHashedLine(String line) {
        String[] parts = line.split(":", -1);
        if (parts.length != 5) {
            return null;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] hash = Base64.getDecoder().decode(parts[4]);
            if (iterations < 1 || salt.length == 0 || hash.length == 0
                    || parts[0].isEmpty()) {
                return null;
            }
            return new Entry(line, parts[0], parts[1], iterations, salt, hash);
        } catch (IllegalArgumentException e) {
            return null; // not numbers / not Base64
        }
    }

    // "username,password": split at the FIRST comma so passwords may
    // contain commas
    private static Entry migrateLegacyLine(String line) {
        int comma = line.indexOf(',');
        if (comma <= 0 || comma == line.length() - 1) {
            return null;
        }
        String username = line.substring(0, comma).trim();
        char[] password = line.substring(comma + 1).toCharArray();

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = hash(password, salt, ITERATIONS);
        Arrays.fill(password, '\0');

        // the old format never stored an email, so it stays empty
        return new Entry(null, username, "", ITERATIONS, salt, hash);
    }

    private static byte[] hash(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        } finally {
            spec.clearPassword();
        }
    }
}
