
package persistance;
import model.Contact;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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

    // Writes the contents of a file.
    @FunctionalInterface
    private interface FileWriterAction {
        void write(FileOutputStream out) throws IOException;
    }

   
    // Contacts (serialized list)

    public static void saveContacts(List<Contact> contacts) {
        saveContacts(contacts, DEFAULT_PATH);
    }

    public static void saveContacts(List<Contact> contacts, Path file) {
        Objects.requireNonNull(contacts, "contacts cannot be null");

        writeAtomically(file, "contacts", out -> {
            ObjectOutputStream oos =
                    new ObjectOutputStream(new BufferedOutputStream(out));
            // Always write a plain ArrayList so the file can be read back
            // regardless of what List implementation was passed in.
            oos.writeObject(new ArrayList<>(contacts));
            oos.flush();
        });
    }

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
            throw new PersistanceException(
                    "Could not access " + file.toAbsolutePath() + ": "
                    + describe(e), e);
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {

            ois.setObjectInputFilter(FILTER);
            Object data = ois.readObject();

            if (!(data instanceof List<?> list)) {
                throw unreadable(file, "it does not contain a contact list",
                        null);
            }

            List<Contact> contacts = new ArrayList<>(list.size());
            for (Object item : list) {
                if (!(item instanceof Contact contact)) {
                    throw unreadable(file, "it contains something that is "
                            + "not a contact", null);
                }
                contacts.add(contact);
            }
            return contacts;

        } catch (IOException e) {
            throw unreadable(file, describe(e), e);
        } catch (ClassNotFoundException e) {
            throw unreadable(file, "unknown class " + e.getMessage(), e);
        }
    }
    
    
    public static List<String> readLines(Path file) {
        Objects.requireNonNull(file, "file cannot be null");
        try {
            if (!Files.exists(file)) {
                return new ArrayList<>();
            }
            return new ArrayList<>(
                    Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new PersistanceException(
                    "Could not read " + file.toAbsolutePath() + ": "
                    + describe(e), e);
        }
    }

    // Replaces the file with these lines, using the same safe write. 
    public static void writeLinesAtomically(Path file, List<String> lines) {
        Objects.requireNonNull(lines, "lines cannot be null");

        writeAtomically(file, "data", out -> {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        });
    }

    // Helpers
   
    private static void writeAtomically(
            Path file, String what, FileWriterAction action) {

        Objects.requireNonNull(file, "file cannot be null");

        Path target = file.toAbsolutePath();
        Path temp = null;

        try {
            temp = Files.createTempFile(
                    target.getParent(),
                    target.getFileName().toString(),
                    ".tmp");

            try (FileOutputStream fos = new FileOutputStream(temp.toFile())) {
                action.write(fos);
                fos.getFD().sync(); // make sure the bytes reached the disk
            }

            moveIntoPlace(temp, target);

        } catch (IOException e) {
            deleteQuietly(temp);
            throw new PersistanceException(
                    "Could not save " + what + " to " + target + ": "
                    + describe(e), e);
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

    // Turns an I/O error into a sentence a user can act on. 
    private static String describe(IOException e) {
        if (e instanceof AccessDeniedException) {
            return "permission denied - the file or folder is read-only "
                    + "or locked by another program";
        }
        if (e instanceof NoSuchFileException) {
            return "the folder does not exist";
        }
        if (e instanceof FileSystemException fse && fse.getReason() != null) {
            return fse.getReason();
        }
        return e.getMessage() == null
                ? e.getClass().getSimpleName() : e.getMessage();
    }

    private static PersistanceException unreadable(
            Path file, String reason, Throwable cause) {

        return new PersistanceException(
                "Could not read contacts from " + file.toAbsolutePath()
                + " (" + reason + "). The file has been left untouched so "
                + "no data is overwritten.", cause);
    }
}