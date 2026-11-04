package service;

import model.Contact;
import persistance.FileHandler;
import java.util.ArrayList;
import java.util.List;

class DuplicateContactException extends Exception {
    public DuplicateContactException(String message) {
        super(message);
    }
}

class InvalidContactException extends Exception {
    public InvalidContactException(String message) {
        super(message);
    }
}

public class ContactManager {
    private List<Contact> contacts;

    public ContactManager() {
        // Load persisted contacts from file on startup
        this.contacts = FileHandler.loadContacts();
        if (this.contacts == null) {
            this.contacts = new ArrayList<>();
        }
    }
 
    public void addContact(Contact c) throws DuplicateContactException, InvalidContactException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new InvalidContactException("Name cannot be empty.");
        }
        
        // Check for duplicates using phone number or ID
        for (Contact existing : contacts) {
            if (existing.getPhone().equals(c.getPhone())) {
                throw new DuplicateContactException("Contact with this phone number already exists.");
            }
        }
        
        contacts.add(c);
        FileHandler.saveContacts(contacts); // Auto-save
        logAction("Added contact: " + c.getName());
    }
   
    public void deleteContact(String name) throws InvalidContactException {
        boolean removed = contacts.removeIf(c -> c.getName().equalsIgnoreCase(name));
        if (!removed) {
            throw new InvalidContactException("Contact not found.");
        }
        FileHandler.saveContacts(contacts); // Auto-save after delete
        logAction("Deleted contact: " + name);
    }

    public List<Contact> viewAllContacts() {
        return new ArrayList<>(contacts);
    }

    private void logAction(String action) {
        System.out.println("[LOG] " + action);
    }
}