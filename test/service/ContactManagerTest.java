package service;

import model.Contact;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import persistance.ActivityLog;
import persistance.ContactRepository;
import persistance.PersistanceException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactManagerTest {

    // In-memory storage so tests never touch contacts.dat. 
    static class MemoryRepo implements ContactRepository {
        List<Contact> data = new ArrayList<>();
        boolean failSaves = false;
        boolean failLoad = false;
        int saves = 0;

        @Override
        public List<Contact> load() {
            if (failLoad) throw new PersistanceException("load failed");
            return new ArrayList<>(data);
        }

        @Override
        public void save(List<Contact> contacts) {
            if (failSaves) throw new PersistanceException("disk full");
            data = new ArrayList<>(contacts);
            saves++;
        }
    }

    @BeforeAll
    static void sendLogToTempFile() {
        ActivityLog.setFile(Paths.get(System.getProperty("java.io.tmpdir"), "cms-test.log"));
    }

    private static ContactManager newManager(MemoryRepo repo) {
        return new ContactManager(repo);
    }

    // adding 

    @Test
    void startsEmpty() {
        ContactManager m = newManager(new MemoryRepo());
        assertEquals(0, m.count());
        assertTrue(m.viewAllContacts().isEmpty());
    }

    @Test
    void addsValidContactAndSaves() throws Exception {
        MemoryRepo repo = new MemoryRepo();
        ContactManager m = newManager(repo);
        m.addContact("Thabo Nkosi", "071 234 5678", "Thabo@Example.com");

        assertEquals(1, m.count());
        assertEquals(1, repo.data.size());
        Contact found = m.findByPhone("0712345678");
        assertNotNull(found);
        assertEquals("thabo@example.com", found.getEmail());
    }

    @Test
    void loadsExistingContactsOnStartup() {
        MemoryRepo repo = new MemoryRepo();
        repo.data.add(new Contact("Existing", "0712345678", "e@example.com"));
        ContactManager m = newManager(repo);
        assertEquals(1, m.count());
        assertNotNull(m.findByPhone("0712345678"));
    }

    @Test
    void startupFailsIfStoredDataUnreadable() {
        MemoryRepo repo = new MemoryRepo();
        repo.failLoad = true;
        assertThrows(PersistanceException.class, () -> newManager(repo));
    }

    @Test
    void rejectsInvalidInput() {
        ContactManager m = newManager(new MemoryRepo());
        assertThrows(InvalidContactException.class,
                () -> m.addContact("", "0712345678", "a@example.com"));
        assertThrows(InvalidContactException.class,
                () -> m.addContact("A", "123", "a@example.com"));
        assertThrows(InvalidContactException.class,
                () -> m.addContact("A", "0712345678", "not-an-email"));
        assertThrows(InvalidContactException.class,
                () -> m.addContact((Contact) null));
        assertEquals(0, m.count());
    }

    @Test
    void rejectsDuplicatePhoneEvenWithDifferentFormatting() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");

        DuplicateContactException ex = assertThrows(DuplicateContactException.class,
                () -> m.addContact("Bob", "071 234 5678", "bob@example.com"));
        assertTrue(ex.getMessage().contains("Alice"));
        assertEquals(1, m.count());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        assertThrows(DuplicateContactException.class,
                () -> m.addContact("Bob", "0823456789", "ALICE@example.com"));
    }

    @Test
    void sameNameIsAllowedWhenPhoneAndEmailDiffer() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("John Smith", "0712345678", "john1@example.com");
        m.addContact("John Smith", "0823456789", "john2@example.com");
        assertEquals(2, m.count());
    }

    // editing 

    @Test
    void updateChangesDetailsAndKeepsIdAndDateAdded() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        Contact before = m.findByPhone("0712345678");

        m.updateContact(before.getId(), "Alice Smith", "0823456789", "alice2@example.com");

        Contact after = m.findById(before.getId());
        assertEquals("Alice Smith", after.getName());
        assertEquals("0823456789", after.getPhone());
        assertEquals(before.getDateAdded(), after.getDateAdded());
        assertFalse(after.getLastModified().isBefore(before.getLastModified()));
        assertNull(m.findByPhone("0712345678"));          // old phone no longer indexed
        assertNotNull(m.findByPhone("0823456789"));
        assertEquals(1, m.count());
    }

    @Test
    void canEditNameOnlyWithoutDuplicateError() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        String id = m.findByPhone("0712345678").getId();
        m.updateContact(id, "Alice B", "0712345678", "alice@example.com");
        assertEquals("Alice B", m.findById(id).getName());
    }

    @Test
    void updateToAnotherContactsPhoneOrEmailIsRejected() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        m.addContact("Bob", "0823456789", "bob@example.com");
        String bobId = m.findByPhone("0823456789").getId();

        assertThrows(DuplicateContactException.class,
                () -> m.updateContact(bobId, "Bob", "0712345678", "bob@example.com"));
        assertThrows(DuplicateContactException.class,
                () -> m.updateContact(bobId, "Bob", "0823456789", "alice@example.com"));
        assertEquals("0823456789", m.findById(bobId).getPhone());   // unchanged
    }

    @Test
    void updateWithInvalidDataIsRejected() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        String id = m.findByPhone("0712345678").getId();
        assertThrows(InvalidContactException.class,
                () -> m.updateContact(id, "", "0712345678", "alice@example.com"));
        assertThrows(InvalidContactException.class,
                () -> m.updateContact(id, "Alice", "12", "alice@example.com"));
    }

    @Test
    void updateOfUnknownIdIsRejected() {
        ContactManager m = newManager(new MemoryRepo());
        assertThrows(InvalidContactException.class,
                () -> m.updateContact("no-such-id", "A", "0712345678", "a@example.com"));
        assertThrows(InvalidContactException.class,
                () -> m.updateContact(null, "A", "0712345678", "a@example.com"));
    }

    @Test
    void unchangedUpdateDoesNotSave() throws Exception {
        MemoryRepo repo = new MemoryRepo();
        ContactManager m = newManager(repo);
        m.addContact("Alice", "0712345678", "alice@example.com");
        int savesBefore = repo.saves;
        String id = m.findByPhone("0712345678").getId();

        m.updateContact(id, "Alice", "0712345678", "alice@example.com");
        assertEquals(savesBefore, repo.saves);
    }

    // deleting 

    @Test
    void deleteRemovesOnlyThatContact() throws Exception {
        MemoryRepo repo = new MemoryRepo();
        ContactManager m = newManager(repo);
        m.addContact("Alice", "0712345678", "alice@example.com");
        m.addContact("Bob", "0823456789", "bob@example.com");

        m.deleteContactById(m.findByPhone("0712345678").getId());

        assertEquals(1, m.count());
        assertEquals(1, repo.data.size());
        assertNull(m.findByPhone("0712345678"));
        assertNotNull(m.findByPhone("0823456789"));
    }

    @Test
    void deletedPhoneAndEmailCanBeReused() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        m.deleteContactById(m.findByPhone("0712345678").getId());
        assertDoesNotThrow(() -> m.addContact("Alice 2", "0712345678", "alice@example.com"));
    }

    @Test
    void deleteOfUnknownIdIsRejected() {
        ContactManager m = newManager(new MemoryRepo());
        assertThrows(InvalidContactException.class, () -> m.deleteContactById("nope"));
        assertThrows(InvalidContactException.class, () -> m.deleteContactById(null));
    }

    // rollback when saving fails 

    @Test
    void failedSaveOnAddRollsBack() {
        MemoryRepo repo = new MemoryRepo();
        repo.failSaves = true;
        ContactManager m = newManager(repo);

        assertThrows(PersistanceException.class,
                () -> m.addContact("Alice", "0712345678", "alice@example.com"));
        assertEquals(0, m.count());
        assertNull(m.findByPhone("0712345678"));
    }

    @Test
    void failedSaveOnEditRollsBack() throws Exception {
        MemoryRepo repo = new MemoryRepo();
        ContactManager m = newManager(repo);
        m.addContact("Alice", "0712345678", "alice@example.com");
        String id = m.findByPhone("0712345678").getId();

        repo.failSaves = true;
        assertThrows(PersistanceException.class,
                () -> m.updateContact(id, "Changed", "0712345678", "alice@example.com"));
        assertEquals("Alice", m.findById(id).getName());
    }

    @Test
    void failedSaveOnDeleteRollsBack() throws Exception {
        MemoryRepo repo = new MemoryRepo();
        ContactManager m = newManager(repo);
        m.addContact("Alice", "0712345678", "alice@example.com");
        String id = m.findByPhone("0712345678").getId();

        repo.failSaves = true;
        assertThrows(PersistanceException.class, () -> m.deleteContactById(id));
        assertEquals(1, m.count());
        assertNotNull(m.findById(id));
    }

    //  reading, searching, sorting

    @Test
    void searchFindsByNamePhoneAndEmail() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice Jones", "0712345678", "alice@work.org");
        m.addContact("Bob Brown", "0823456789", "bob@example.com");

        assertEquals(1, m.search("jones").size());
        assertEquals(1, m.search("0823").size());
        assertEquals(1, m.search("work.org").size());
        assertEquals(0, m.search("zzz").size());
        assertEquals(0, m.search("JONES", false).size());   // case-sensitive option
    }

    @Test
    void searchOnEmptyListReturnsNothing() {
        assertTrue(newManager(new MemoryRepo()).search("anything").isEmpty());
    }

    @Test
    void sortedViewIsAlphabeticalAndIgnoresCase() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("charlie", "0712345678", "c@example.com");
        m.addContact("Alice", "0823456789", "a@example.com");
        m.addContact("bob", "0834567890", "b@example.com");

        List<Contact> sorted = m.viewAllContactsSorted();
        assertEquals("Alice", sorted.get(0).getName());
        assertEquals("bob", sorted.get(1).getName());
        assertEquals("charlie", sorted.get(2).getName());
    }

    @Test
    void viewAllReturnsACopy() throws Exception {
        ContactManager m = newManager(new MemoryRepo());
        m.addContact("Alice", "0712345678", "alice@example.com");
        List<Contact> copy = m.viewAllContacts();
        copy.clear();
        assertEquals(1, m.count());
    }

    @Test
    void findByIdAndPhoneReturnNullWhenMissing() {
        ContactManager m = newManager(new MemoryRepo());
        assertNull(m.findById("x"));
        assertNull(m.findById(null));
        assertNull(m.findByPhone("0000000000"));
    }
}