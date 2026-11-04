package service;

import model.Contact;
import persistance.FileHandler;
import persistance.PersistenceException;
import search.ContactSearch;
import search.Contactindex;
import search.Contactsort;
import java.util.ArrayList;
import java.util.List;

/**
 * The single place the rest of the program uses to read, change, save and
 * search contacts. The UI should go through this class instead of calling
 * FileHandler directly.
 *
 * - Every change is saved to disk straight away.
 * - If a save fails, the change is undone in memory and the
 *   PersistenceException is rethrown, so what you see on screen never
 *   differs from what is on disk.
 * - If the data file exists but cannot be read, the constructor throws a
 *   PersistenceException and nothing is ever written over that file.
 * - Methods are synchronized, so one instance can be shared safely.
 *
 * Use ContactManager.getInstance() so all windows share one list.
 */
public class ContactManager {

    private static ContactManager instance;

    private final List<Contact> contacts;
    private final Contactindex phoneIndex = new Contactindex();

    /** Returns the shared manager, creating it (and loading the file) once. */
    public static synchronized ContactManager getInstance() {
        if (instance == null) {
            instance = new ContactManager();
        }
        return instance;
    }

    /**
     * Loads the saved contacts.
     *
     * @throws PersistenceException if the data file exists but is unreadable
     */
    public ContactManager() {
        this.contacts = new ArrayList<>(FileHandler.loadContacts());
        this.phoneIndex.rebuild(contacts);
    }

    // --------------------------------------------------
    // Changing contacts
    // --------------------------------------------------

    /**
     * Adds a contact and saves it.
     *
     * @throws InvalidContactException   if the contact is null
     * @throws DuplicateContactException if the phone number is already used
     * @throws PersistenceException      if saving fails (nothing is added)
     */
    public synchronized void addContact(Contact c)
            throws DuplicateContactException, InvalidContactException {

        if (c == null) {
            throw new InvalidContactException("Contact cannot be null.");
        }

        // Fast duplicate check through the phone index; phone numbers are
        // normalized, so "071 234 5678" and "0712345678" count as the same.
        if (phoneIndex.findByExactPhone(c.getPhone()) != null) {
            throw new DuplicateContactException(
                    "A contact with this phone number already exists.");
        }

        contacts.add(c);

        try {
            FileHandler.saveContacts(contacts);
        } catch (PersistenceException e) {
            contacts.remove(contacts.size() - 1); // undo: keep memory == disk
            throw e;
        }

        phoneIndex.onAdd(c);
        logAction("Added contact: " + c.getName());
    }

    /**
     * Deletes the contact with this id and saves. Deleting by id removes
     * exactly one contact, even when several people share a name.
     *
     * @throws InvalidContactException if no contact has this id
     * @throws PersistenceException    if saving fails (nothing is deleted)
     */
    public synchronized void deleteContactById(String id)
            throws InvalidContactException {

        int index = indexOfId(id);

        if (index < 0) {
            throw new InvalidContactException("Contact not found.");
        }

        Contact removed = contacts.remove(index);

        try {
            FileHandler.saveContacts(contacts);
        } catch (PersistenceException e) {
            contacts.add(index, removed); // undo: put it back where it was
            throw e;
        }

        phoneIndex.onRemove(removed);
        logAction("Deleted contact: " + removed.getName());
    }

    private int indexOfId(String id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    // --------------------------------------------------
    // Reading and searching (these return copies, so callers can sort
    // or filter the result without touching the manager's own list)
    // --------------------------------------------------

    public synchronized List<Contact> viewAllContacts() {
        return new ArrayList<>(contacts);
    }

    /** All contacts, sorted A-Z by name. */
    public synchronized List<Contact> viewAllContactsSorted() {
        List<Contact> copy = new ArrayList<>(contacts);
        Contactsort.sortByName(copy);
        return copy;
    }

    /** Contacts whose name contains the query. */
    public synchronized List<Contact> search(String query) {
        return ContactSearch.searchByName(contacts, query);
    }

    /** Exact phone lookup (O(1)); returns null when there is no match. */
    public synchronized Contact findByPhone(String phone) {
        return phoneIndex.findByExactPhone(phone);
    }

    private void logAction(String action) {
        System.out.println("[LOG] " + action);
    }
}