/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistance;

import model.Contact;
import java.nio.file.Path;
import java.util.List;

/** Stores contacts in a file, using FileHandler's safe save and load. */
public class FileContactRepository implements ContactRepository {

    private final Path file; // null means "use FileHandler's default file"

    /** Uses the default file (contacts.dat, or -Dcontacts.file=...). */
    public FileContactRepository() {
        this.file = null;
    }

    public FileContactRepository(Path file) {
        this.file = java.util.Objects.requireNonNull(file, "file");
    }

    @Override
    public List<Contact> load() {
        return file == null
                ? FileHandler.loadContacts()
                : FileHandler.loadContacts(file);
    }

    @Override
    public void save(List<Contact> contacts) {
        if (file == null) {
            FileHandler.saveContacts(contacts);
        } else {
            FileHandler.saveContacts(contacts, file);
        }
    }
}