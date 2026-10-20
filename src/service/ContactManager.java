package service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


class Contact implements Serializable {
    private String name;
    private String phone;
    private String email;

    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    
    public String displayContact() {
        return "Name: " + name + " | Phone: " + phone + " | Email: " + email;
    }
   
    public boolean isSameContact(Contact other) {
        if (other == null) return false;
        return this.name.equalsIgnoreCase(other.name) && this.phone.equals(other.phone);
    }
}

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
        contacts = new ArrayList<>();
    }
 
    public void addContact(Contact c) throws DuplicateContactException, InvalidContactException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new InvalidContactException("Name cannot be empty.");
        }
        if (!isValidEmail(c.getEmail())) {
            throw new InvalidContactException("Invalid email format.");
        }
        if (!isValidPhone(c.getPhone())) {
            throw new InvalidContactException("Invalid phone number format.");
        }
        for (Contact existing : contacts) {
            if (existing.isSameContact(c)) {
                throw new DuplicateContactException("Contact already exists.");
            }
        }
        contacts.add(c);
        logAction("Added contact: " + c.displayContact());
    }
 
    public void updateContact(Contact oldContact, Contact newContact) throws InvalidContactException {
        int index = -1;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).isSameContact(oldContact)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new InvalidContactException("Contact not found.");
        }
        contacts.set(index, newContact);
        logAction("Updated contact: " + newContact.displayContact());
    }
   
    public void deleteContact(String name) throws InvalidContactException {
        boolean removed = contacts.removeIf(c -> c.getName().equalsIgnoreCase(name));
        if (!removed) {
            throw new InvalidContactException("Contact not found.");
        }
        logAction("Deleted contact: " + name);
    }

    public List<Contact> viewAllContacts() {
        List<Contact> copy = new ArrayList<>();
        for (Contact c : contacts) {
            System.out.println(c.displayContact());
            copy.add(c);
        }
        return copy;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    private void logAction(String action) {
        System.out.println("[LOG] " + action);
    }
}
