package service;

import model.Contact;
import persistance.ActivityLog;
import persistance.ContactRepository;
import persistance.FileContactRepository;
import persistance.PersistanceException;
import search.ContactSearch;
import search.Contactindex;
import search.Contactsort;
import search.Concurrentcontactsearch;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class ContactManager {

    // Threaded search only pays off on big lists (thread start-up cost)
    private static final int CONCURRENT_THRESHOLD = 5000;
    private static ContactManager instance;

    private final ContactRepository repository;
    private final List<Contact> contacts;
    private final Contactindex phoneIndex = new Contactindex();

    // Returns the shared manager, creating it (and loading the file) once. 
    public static synchronized ContactManager getInstance() {
        if (instance == null) {
            instance = new ContactManager();
        }
        return instance;
    }

    // Uses the default contacts file.
     
     // @throws PersistenceException if the data file exists but is unreadable
     
    public ContactManager() {
        this(new FileContactRepository());
    }

    // Uses any storage (for example an in-memory one for tests).
     
     // @throws PersistenceException if the stored data cannot be read
     
    public ContactManager(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.contacts = new ArrayList<>(repository.load());
        this.phoneIndex.rebuild(contacts);
    }


    // Adding

    //Validates the details, then adds and saves the contact.
     
     // @throws InvalidContactException   if the name, phone or email is invalid
     // @throws DuplicateContactException if the phone or email is already used
     // @throws PersistenceException      if saving fails (nothing is added)
     
    public synchronized void addContact(String name, String phone, String email)
            throws InvalidContactException, DuplicateContactException {

        Contact contact;
        try {
            contact = new Contact(name, phone, email);
        } catch (IllegalArgumentException e) {
            ActivityLog.log("ADD_REJECTED", e.getMessage()
                    + " [" + name + ", " + phone + ", " + email + "]");
            throw new InvalidContactException(e.getMessage());
        }

        addContact(contact);
    }

    // Adds and saves an already-validated contact.
     
     // @throws InvalidContactException   if the contact is null
     // @throws DuplicateContactException if the phone or email is already used
     // @throws PersistenceException      if saving fails (nothing is added)
     
    public synchronized void addContact(Contact c)
            throws InvalidContactException, DuplicateContactException {

        if (c == null) {
            ActivityLog.log("ADD_REJECTED", "Contact was null");
            throw new InvalidContactException("Contact cannot be null.");
        }

        try {
            checkPhoneIsFree(c, null);
            checkEmailIsFree(c, null);
        } catch (DuplicateContactException e) {
            ActivityLog.log("ADD_REJECTED", e.getMessage()
                    + " [" + c.getName() + ", " + c.getPhone() + "]");
            throw e;
        }

        contacts.add(c);

        try {
            repository.save(contacts);
        } catch (PersistanceException e) {
            contacts.remove(contacts.size() - 1); // undo: keep memory == disk
            ActivityLog.log("ADD_FAILED", e.getMessage());
            throw e;
        }

        phoneIndex.onAdd(c);
        ActivityLog.log("ADD", c.getName() + " (" + c.getPhone() + ")");
    }

   
    // Editing

    /**
     * Changes the details of the contact with this id and saves. The id and
     * the date added stay the same; the last-modified time is updated.
     * Nothing is saved when the details are unchanged.
     *
     * @throws InvalidContactException   if there is no such contact, or the
     *                                   new name, phone or email is invalid
     * @throws DuplicateContactException if the new phone or email already
     *                                   belongs to a different contact
     * @throws PersistenceException      if saving fails (nothing is changed)
     */
    public synchronized void updateContact(
            String id, String name, String phone, String email)
            throws InvalidContactException, DuplicateContactException {

        int index = indexOfId(id);
        if (index < 0) {
            ActivityLog.log("EDIT_REJECTED", "No contact with id " + id);
            throw new InvalidContactException("Contact not found.");
        }

        Contact old = contacts.get(index);

        Contact updated;
        try {
            updated = new Contact(old.getId(), name, phone, email,
                    old.getDateAdded(), LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            ActivityLog.log("EDIT_REJECTED", e.getMessage()
                    + " [" + old.getName() + "]");
            throw new InvalidContactException(e.getMessage());
        }

        boolean phoneChanged = !updated.getPhone().equals(old.getPhone());
        boolean emailChanged = !updated.getEmail().equals(old.getEmail());
        boolean nameChanged = !updated.getName().equals(old.getName());

        if (!phoneChanged && !emailChanged && !nameChanged) {
            return; // nothing to do
        }

        // Only re-check what actually changed, so a contact whose unchanged
        // phone or email is already shared (old data) can still be edited.
        try {
            if (phoneChanged) {
                checkPhoneIsFree(updated, old.getId());
            }
            if (emailChanged) {
                checkEmailIsFree(updated, old.getId());
            }
        } catch (DuplicateContactException e) {
            ActivityLog.log("EDIT_REJECTED", e.getMessage()
                    + " [" + old.getName() + "]");
            throw e;
        }

        contacts.set(index, updated);

        try {
            repository.save(contacts);
        } catch (PersistanceException e) {
            contacts.set(index, old); // undo: put the old details back
            ActivityLog.log("EDIT_FAILED", e.getMessage());
            throw e;
        }

        phoneIndex.onRemove(old);
        phoneIndex.onAdd(updated);
        ActivityLog.log("EDIT", old.getName() + " (" + old.getPhone() + ") -> "
                + updated.getName() + " (" + updated.getPhone() + ")");
    }

    
    // Deleting
   

    /**
     * Deletes the contact with this id and saves. Deleting by id removes
     * exactly one contact, even when several people share a name.
     *
     * @throws InvalidContactException if no contact has this id
     * @throws PersistenceException    if saving fails (nothing is deleted)
     */
    
    /** Name search; uses threads on large lists. Run from a SwingWorker in the GUI. */
    public List<Contact> searchByName(String query) throws InterruptedException {
       List<Contact> snapshot = viewAllContacts(); // synchronized copy, safe to share with threads
       if (snapshot.size() < CONCURRENT_THRESHOLD) {
           return ContactSearch.searchByName(snapshot, query);
       }
       return Concurrentcontactsearch.searchByNameConcurrent(
               snapshot, query, Runtime.getRuntime().availableProcessors());
    }
    
    
    /** All contacts sorted A-Z with your own merge sort: O(n log n), stable. */
    public synchronized List<Contact> viewAllContactsMergeSorted() {
        List<Contact> copy = new ArrayList<>(contacts);
        Contactsort.mergeSortByName(copy);
        return copy;
    }
    
    
    /** Exact name lookup: sort once, then binary search O(log n). */
    public Contact findByExactName(List<Contact> sortedByName, String name) {
        int i = ContactSearch.binarySearchByName(sortedByName, name);
        return i < 0 ? null : sortedByName.get(i);
    }
    
    
    public synchronized void deleteContactById(String id)
            throws InvalidContactException {

        int index = indexOfId(id);

        if (index < 0) {
            ActivityLog.log("DELETE_REJECTED", "No contact with id " + id);
            throw new InvalidContactException("Contact not found.");
        }

        Contact removed = contacts.remove(index);

        try {
            repository.save(contacts);
        } catch (PersistanceException e) {
            contacts.add(index, removed); // undo: put it back where it was
            ActivityLog.log("DELETE_FAILED", e.getMessage());
            throw e;
        }

        phoneIndex.onRemove(removed);
        ActivityLog.log("DELETE",
                removed.getName() + " (" + removed.getPhone() + ")");
    }
    

    public synchronized List<Contact> viewAllContacts() {
        return new ArrayList<>(contacts);
    }

    // All contacts, sorted A-Z by name. 
    public synchronized List<Contact> viewAllContactsSorted() {
        return viewAllContactsSorted(Contact.BY_NAME);
    }

    // All contacts in the given order, e.g. Contact.BY_DATE_ADDED. 
    public synchronized List<Contact> viewAllContactsSorted(
            Comparator<Contact> order) {
        List<Contact> copy = new ArrayList<>(contacts);
        copy.sort(order);
        return copy;
    }

    // Contacts whose name, phone or email contains the query. 
    public synchronized List<Contact> search(String query) {
        return ContactSearch.search(contacts, query);
    }

    // Same, with a choice of ignoring upper/lower case. 
    public synchronized List<Contact> search(String query, boolean ignoreCase) {
        return ContactSearch.search(contacts, query, ignoreCase);
    }

    // Exact phone lookup (O(1)); returns null when there is no match. 
    public synchronized Contact findByPhone(String phone) {
        return phoneIndex.findByExactPhone(phone);
    }

    // Returns the contact with this id, or null. 
    public synchronized Contact findById(String id) {
        int index = indexOfId(id);
        return index < 0 ? null : contacts.get(index);
    }

    public synchronized int count() {
        return contacts.size();
    }

    
    // Helpers
    

    private void checkPhoneIsFree(Contact candidate, String ignoreId)
            throws DuplicateContactException {

        Contact holder = phoneIndex.findByExactPhone(candidate.getPhone());

        if (holder != null && !holder.getId().equals(ignoreId)) {
            throw new DuplicateContactException(
                    "A contact with this phone number already exists ("
                    + holder.getName() + ").");
        }
    }

    private void checkEmailIsFree(Contact candidate, String ignoreId)
            throws DuplicateContactException {

        for (Contact other : contacts) {
            if (other.getEmail().equals(candidate.getEmail())
                    && !other.getId().equals(ignoreId)) {
                throw new DuplicateContactException(
                        "A contact with this email address already exists ("
                        + other.getName() + ").");
            }
        }
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
}