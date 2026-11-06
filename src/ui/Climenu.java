package ui;

import java.util.Scanner;
import java.util.List;
import model.Contact;
import persistance.PersistanceException;
import service.ContactManager;
import service.DuplicateContactException;
import service.InvalidContactException;



public class Climenu {
   
   
   private static final Scanner input = new Scanner(System.in);
    private static ContactManager manager;

    public static void main(String[] args) {
        try {
            manager = ContactManager.getInstance();
        } catch (PersistanceException e) {
            System.out.println("Could not load the saved contacts: " + e.getMessage());
            return;
        }

        System.out.print("\n _________________________________________________");
        System.out.print("\n|                                                 |");
        System.out.println("\n|_____WELCOME TO CONTACT MANAGEMENT SYSTEM________|");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            if (!input.hasNextLine()) {
                break;
            }
            String choice = input.nextLine().trim();

            try {
                switch (choice) {
                    case "1": addContact();      break;
                    case "2": viewContacts();    break;
                    case "3": editContact();     break;
                    case "4": deleteContact();   break;
                    case "5": searchContacts();  break;
                    case "6": sortContacts();    break;
                    case "7":
                        System.out.println("Thank you for using Contact Management, Bye.");
                        running = false;
                        break;
                    default:
                        System.out.println("INVALID INPUT, TRY AGAIN");
                }
            } catch (PersistanceException e) {
                System.out.println("Could not save your change: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.print("\n*================== CLI MENU ====================*\n");
        System.out.print("* 1. ADD                                 2. VIEW  *\n");
        System.out.print("* 3. EDIT                                4. DELETE*\n");
        System.out.print("* 5. SEARCH                              6. SORT  *\n");
        System.out.print("* 7. EXIT                                         *\n");
        System.out.print("***************************************************\n");
    }

    private static void addContact() {
        System.out.println("Welcome ADD your contact: ");
        String name = ask("Name and Surname: ");
        String phone = ask("Phone number: ");
        String email = ask("Email: ");

        try {
            manager.addContact(name, phone, email);
            System.out.println("Contact added.");
        } catch (InvalidContactException | DuplicateContactException e) {
            System.out.println("Could not add: " + e.getMessage());
        }
    }

    private static void viewContacts() {
        show(manager.viewAllContacts());
    }

    private static void editContact() {
        System.out.println("Welcome EDIT the contact: ");
        List<Contact> list = manager.viewAllContacts();
        if (list.isEmpty()) {
            System.out.println("No contacts added.");
            return;
        }
        show(list);
        Contact selected = pick(list, "Number of the contact to edit: ");
        if (selected == null) {
            return;
        }

        System.out.println("Press Enter to keep the current value.");
        String name = askKeep("Name [" + selected.getName() + "]: ", selected.getName());
        String phone = askKeep("Phone [" + selected.getPhone() + "]: ", selected.getPhone());
        String email = askKeep("Email [" + selected.getEmail() + "]: ", selected.getEmail());

        try {
            manager.updateContact(selected.getId(), name, phone, email);
            System.out.println("Contact updated.");
        } catch (InvalidContactException | DuplicateContactException e) {
            System.out.println("Could not update: " + e.getMessage());
        }
    }

    private static void deleteContact() {
        List<Contact> list = manager.viewAllContacts();
        if (list.isEmpty()) {
            System.out.println("No contacts added.");
            return;
        }
        show(list);
        Contact selected = pick(list, "Number of the contact to delete: ");
        if (selected == null) {
            return;
        }

        String sure = ask("Are you sure you want to delete " + selected.getName() + "? [yes/No]: ");
        if (!sure.equalsIgnoreCase("yes")) {
            System.out.println("Delete cancelled.");
            return;
        }

        try {
            manager.deleteContactById(selected.getId());
            System.out.println("Contact deleted.");
        } catch (InvalidContactException e) {
            System.out.println("Could not delete: " + e.getMessage());
        }
    }

    private static void searchContacts() {
        String query = ask("Search (name, phone or email): ");
        if (query.isEmpty()) {
            System.out.println("Nothing to search for.");
            return;
        }
        List<Contact> found = manager.search(query);
        if (found.isEmpty()) {
            System.out.println("No contacts match \"" + query + "\".");
        } else {
            System.out.println(found.size() + " match(es):");
            show(found);
        }
    }

    private static void sortContacts() {
        String how = ask("Sort by 1. Name  2. Phone: ");
        if (how.equals("1")) {
            show(manager.viewAllContactsSorted());
        } else if (how.equals("2")) {
            show(manager.viewAllContactsSorted(Contact.BY_PHONE));
        } else {
            System.out.println("INVALID INPUT, TRY AGAIN");
        }
    }

    private static void show(List<Contact> list) {
        if (list.isEmpty()) {
            System.out.println("No contacts added.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Contact c = list.get(i);
            System.out.println((i + 1) + ". Name: " + c.getName()
                    + " | Phone: " + c.getPhone()
                    + " | Email: " + c.getEmail());
        }
    }

    private static Contact pick(List<Contact> list, String prompt) {
        String text = ask(prompt);
        try {
            int index = Integer.parseInt(text) - 1;
            if (index >= 0 && index < list.size()) {
                return list.get(index);
            }
        } catch (NumberFormatException ignored) {
        }
        System.out.println("Invalid number.");
        return null;
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine().trim() : "";
    }

    private static String askKeep(String prompt, String current) {
        String value = ask(prompt);
        return value.isEmpty() ? current : value;
    }
}