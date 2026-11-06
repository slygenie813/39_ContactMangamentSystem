package persistance;

import model.Contact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @TempDir
    Path dir;

    private static List<Contact> sample() {
        List<Contact> l = new ArrayList<>();
        l.add(new Contact("Thabo Nkosi",  "0712345678", "thabo@example.com"));
        l.add(new Contact("Jane Doe",     "0845678901", "jane@example.com"));
        l.add(new Contact("Zoë O'Brien",  "+27823456789", "zoe@example.com"));
        return l;
    }

    private static void writeObject(Path file, Object o) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(o);
        }
    }

    // ---------- normal cases ----------

    @Test
    void saveThenLoadGivesSameContacts() {
        Path file = dir.resolve("contacts.dat");
        List<Contact> original = sample();
        FileHandler.saveContacts(original, file);

        List<Contact> loaded = FileHandler.loadContacts(file);
        assertEquals(original.size(), loaded.size());
        for (int i = 0; i < original.size(); i++) {
            Contact a = original.get(i);
            Contact b = loaded.get(i);
            assertEquals(a.getId(), b.getId());
            assertEquals(a.getName(), b.getName());
            assertEquals(a.getPhone(), b.getPhone());
            assertEquals(a.getEmail(), b.getEmail());
            assertEquals(a.getDateAdded(), b.getDateAdded());
            assertEquals(a.getLastModified(), b.getLastModified());
        }
    }

    @Test
    void savingOverwritesPreviousData() {
        Path file = dir.resolve("contacts.dat");
        FileHandler.saveContacts(sample(), file);
        FileHandler.saveContacts(sample().subList(0, 1), file);
        assertEquals(1, FileHandler.loadContacts(file).size());
    }

    @Test
    void savingAnEmptyListWorks() {
        Path file = dir.resolve("contacts.dat");
        FileHandler.saveContacts(new ArrayList<>(), file);
        assertTrue(FileHandler.loadContacts(file).isEmpty());
    }

    @Test
    void noTemporaryFilesAreLeftBehind() throws IOException {
        Path file = dir.resolve("contacts.dat");
        FileHandler.saveContacts(sample(), file);
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    // ---------- first run / empty ----------

    @Test
    void missingFileMeansNoContactsYet() {
        assertTrue(FileHandler.loadContacts(dir.resolve("missing.dat")).isEmpty());
    }

    @Test
    void emptyFileMeansNoContacts() throws IOException {
        Path file = dir.resolve("empty.dat");
        Files.createFile(file);
        assertTrue(FileHandler.loadContacts(file).isEmpty());
    }

    // ---------- corrupted / hostile files ----------

    @Test
    void garbageFileIsReportedAndLeftUntouched() throws IOException {
        Path file = dir.resolve("garbage.dat");
        byte[] junk = "this is definitely not a serialized list".getBytes();
        Files.write(file, junk);

        assertThrows(PersistenceException.class, () -> FileHandler.loadContacts(file));
        assertArrayEquals(junk, Files.readAllBytes(file));      // nothing overwritten
    }

    @Test
    void truncatedFileIsReported() throws IOException {
        Path file = dir.resolve("contacts.dat");
        FileHandler.saveContacts(sample(), file);
        byte[] bytes = Files.readAllBytes(file);
        Files.write(file, Arrays.copyOf(bytes, bytes.length / 2));

        assertThrows(PersistenceException.class, () -> FileHandler.loadContacts(file));
    }

    @Test
    void listOfWrongThingsIsRejected() throws IOException {
        Path file = dir.resolve("wrong.dat");
        ArrayList<String> notContacts = new ArrayList<>();
        notContacts.add("hello");
        writeObject(file, notContacts);

        assertThrows(PersistenceException.class, () -> FileHandler.loadContacts(file));
    }

    @Test
    void deserialisationFilterRejectsUnexpectedClasses() throws IOException {
        Path file = dir.resolve("hostile.dat");
        writeObject(file, new HashMap<String, String>());

        assertThrows(PersistenceException.class, () -> FileHandler.loadContacts(file));
    }

    // ---------- I/O errors ----------

    @Test
    void savingToAMissingFolderFails() {
        Path file = dir.resolve("no").resolve("such").resolve("folder").resolve("contacts.dat");
        assertThrows(PersistenceException.class, () -> FileHandler.saveContacts(sample(), file));
    }

    // ---------- text lines (used by the accounts file) ----------

    @Test
    void writeAndReadLinesRoundTrip() {
        Path file = dir.resolve("lines.txt");
        FileHandler.writeLinesAtomically(file, List.of("one", "two", "three"));
        assertEquals(List.of("one", "two", "three"), FileHandler.readLines(file));
    }

    @Test
    void readLinesOfMissingFileIsEmpty() {
        assertTrue(FileHandler.readLines(dir.resolve("none.txt")).isEmpty());
    }

    // ---------- repository wrapper ----------

    @Test
    void fileContactRepositoryRoundTrip() {
        FileContactRepository repo = new FileContactRepository(dir.resolve("repo.dat"));
        assertTrue(repo.load().isEmpty());
        repo.save(sample());
        assertEquals(3, repo.load().size());
    }
}