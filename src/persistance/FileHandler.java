package persistance;

import model.Contact;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Saves and loads the contact list.
 *
 * Guarantees:
 *  - A save never leaves a half-written file behind: data is written to a
 *    temporary file first and then moved over the real file.
 *  - Failures are reported with a PersistenceException; they are never
 *    swallowed.
 *  - A file that exists but cannot be read is never treated as "no
 *    contacts". It is left untouched and an exception is thrown, so the
 *    next save cannot overwrite data that might still be recoverable.
 *  - Only the classes a contact list needs can be deserialized.
 *
 * The default file is "contacts.dat" in the working directory. Set the
 * system property "contacts.file" (e.g. -Dcontacts.file=C:/data/c.dat) to
 * use another location. The overloads taking a Path are mainly for tests.
 */
public class FileHandler {

    private static final Path DEFAULT_PATH =
            Paths.get(System.getProperty("contacts.file", "contacts.dat"));

    // Only allow what a saved contact list contains; reject everything else.
    // java.lang.Object is needed because ArrayList checks an Object[] while
    // reading; each element inside is still checked against this list.
    private static final ObjectInputFilter FILTER =
            ObjectInputFilter.Config.createFilter(
                    "java.util.ArrayList;java.time.*;java.lang.String;"
                    + "java.lang.Object;model.Contact;!*");

    private FileHandler() {
        // static utility class
    }

    // --------------------------------------------------
    // Saving
    // --------------------------------------------------

    public static void saveContacts(List<Contact> contacts) {
        saveContacts(contacts, DEFAULT_PATH);
    }

    public static void saveContacts(List<Contact> contacts, Path file) {
        Objects.requireNonNull(contacts, "contacts cannot be null");
        Objects.requireNonNull(file, "file cannot be null");

        Path target = file.toAbsolutePath();
        Path temp = null;

        try {
            temp = Files.createTempFile(
                    target.getParent(),
                    target.getFileName().toString(),
                    ".tmp");

            try (FileOutputStream fos = new FileOutputStream(temp.toFile());
                 ObjectOutputStream oos = new ObjectOutputStream(
                         new BufferedOutputStream(fos))) {

                // Always write a plain ArrayList so the file can be read
                // back regardless of what List implementation was passed in.
                oos.writeObject(new ArrayList<>(contacts));
                oos.flush();
                fos.getFD().sync(); // make sure the bytes reached the disk
            }

            moveIntoPlace(temp, target);

        } catch (IOException e) {
            deleteQuietly(temp);
            throw new PersistenceException(
                    "Could not save contacts to " + target + ": "
                    + e.getMessage(), e);
        }
    }

    private static void moveIntoPlace(Path temp, Path target)
            throws IOException {
        try {
            Files.move(temp, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // nothing more we can do; the original error is more useful
        }
    }

    // --------------------------------------------------
    // Loading
    // --------------------------------------------------

    public static List<Contact> loadContacts() {
        return loadContacts(DEFAULT_PATH);
    }

    public static List<Contact> loadContacts(Path file) {
        Objects.requireNonNull(file, "file cannot be null");

        try {
            // First run: no file yet means no contacts yet.
            if (!Files.exists(file) || Files.size(file) == 0) {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Could not access " + file.toAbsolutePath() + ": "
                    + e.getMessage(), e);
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {

            ois.setObjectInputFilter(FILTER);
            Object data = ois.readObject();

            if (!(data instanceof List<?> list)) {
                throw unreadable(file, "it does not contain a contact list");
            }

            List<Contact> contacts = new ArrayList<>(list.size());
            for (Object item : list) {
                if (!(item instanceof Contact contact)) {
                    throw unreadable(file, "it contains something that is "
                            + "not a contact");
                }
                contacts.add(contact);
            }
            return contacts;

        } catch (IOException | ClassNotFoundException e) {
            throw unreadable(file, e.getMessage(), e);
        }
    }

    private static PersistenceException unreadable(Path file, String reason) {
        return unreadable(file, reason, null);
    }

    private static PersistenceException unreadable(
            Path file, String reason, Throwable cause) {

        return new PersistenceException(
                "Could not read contacts from " + file.toAbsolutePath()
                + " (" + reason + "). The file has been left untouched so "
                + "no data is overwritten.", cause);
    }
}