package search;

import model.Contact;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactIndexTest {

    private static Contact contact(String name, String phone, String email) {
        return new Contact(name, phone, email);
    }

    @Test
    void rebuildThenFindExactPhone() {
        List<Contact> list = new ArrayList<>();
        Contact a = contact("Alice", "0712345678", "a@example.com");
        Contact b = contact("Bob", "0823456789", "b@example.com");
        list.add(a);
        list.add(b);

        Contactindex idx = new Contactindex();
        idx.rebuild(list);

        assertEquals(2, idx.size());
        assertEquals(a, idx.findByExactPhone("0712345678"));
        assertEquals(b, idx.findByExactPhone("082 345 6789"));   // formatting ignored
    }

    @Test
    void unknownPhoneReturnsNull() {
        Contactindex idx = new Contactindex();
        assertNull(idx.findByExactPhone("0000000000"));
        assertNull(idx.findByExactPhone(null));
    }

    @Test
    void onAddAndOnRemove() {
        Contactindex idx = new Contactindex();
        Contact a = contact("Alice", "0712345678", "a@example.com");

        idx.onAdd(a);
        assertEquals(a, idx.findByExactPhone("0712345678"));

        idx.onRemove(a);
        assertNull(idx.findByExactPhone("0712345678"));
        assertEquals(0, idx.size());
    }

    @Test
    void removingOneContactNeverDropsAnotherContactsEntry() {
        LocalDateTime t = LocalDateTime.now();
        Contact first  = new Contact("id-1", "First",  "0712345678", "f@example.com", t, t);
        Contact second = new Contact("id-2", "Second", "0712345678", "s@example.com", t, t);

        List<Contact> both = new ArrayList<>();
        both.add(first);
        both.add(second);

        Contactindex idx = new Contactindex();
        idx.rebuild(both);                       // later contact wins the shared key
        idx.onRemove(first);                     // first is not the stored holder

        assertEquals(second, idx.findByExactPhone("0712345678"));
    }

    @Test
    void rebuildReplacesOldContents() {
        Contactindex idx = new Contactindex();
        idx.onAdd(contact("Old", "0712345678", "o@example.com"));

        List<Contact> fresh = new ArrayList<>();
        fresh.add(contact("New", "0823456789", "n@example.com"));
        idx.rebuild(fresh);

        assertNull(idx.findByExactPhone("0712345678"));
        assertNotNull(idx.findByExactPhone("0823456789"));
    }
}