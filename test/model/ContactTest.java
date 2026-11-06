package model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {

    @Test
    void createsValidContactAndNormalisesFields() {
        Contact c = new Contact("  Thabo Nkosi ", "071 234-5678", "THABO@Example.com");
        assertEquals("Thabo Nkosi", c.getName());
        assertEquals("0712345678", c.getPhone());
        assertEquals("thabo@example.com", c.getEmail());
        assertNotNull(c.getId());
        assertEquals(c.getDateAdded(), c.getLastModified());
    }

    @Test
    void acceptsInternationalPhone() {
        Contact c = new Contact("A", "+27 71 234 5678", "a@example.com");
        assertEquals("+27712345678", c.getPhone());
    }

    @Test
    void rejectsEmptyOrNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("   ", "0712345678", "a@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("", "0712345678", "a@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, "0712345678", "a@example.com"));
    }

    @Test
    void rejectsBadPhoneNumbers() {
        String[] bad = {"", "12345", "abc1234567", "1234567890123456", "07123-ABC-78"};
        for (String p : bad) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Contact("A", p, "a@example.com"), "phone: " + p);
        }
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("A", null, "a@example.com"));
    }

    @Test
    void rejectsBadEmails() {
        String[] bad = {"", "plain", "a@b", "a@@example.com", "a b@example.com", "@example.com"};
        for (String e : bad) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Contact("A", "0712345678", e), "email: " + e);
        }
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("A", "0712345678", null));
    }

    @Test
    void acceptsVeryLongNameAndSpecialCharacters() {
        String longName = "x".repeat(500);
        assertEquals(longName, new Contact(longName, "0712345678", "a@example.com").getName());
        assertEquals("Zoë O'Brien-Smith",
                new Contact("Zoë O'Brien-Smith", "0712345678", "a@example.com").getName());
    }

    @Test
    void settersValidateAndUpdateLastModified() {
        LocalDateTime added = LocalDateTime.of(2020, 1, 1, 10, 0);
        Contact c = new Contact("id-1", "Old Name", "0712345678", "a@example.com", added, added);

        c.setName("New Name");
        assertEquals("New Name", c.getName());
        assertTrue(c.getLastModified().isAfter(added));

        assertThrows(IllegalArgumentException.class, () -> c.setName("  "));
        assertEquals("New Name", c.getName());                 // unchanged after rejection

        assertThrows(IllegalArgumentException.class, () -> c.setEmail("bad"));
        assertThrows(IllegalArgumentException.class, () -> c.setPhoneNumber("1"));
        assertEquals("a@example.com", c.getEmail());
        assertEquals("0712345678", c.getPhone());
    }

    @Test
    void settingSameValueDoesNotChangeLastModified() {
        LocalDateTime added = LocalDateTime.of(2020, 1, 1, 10, 0);
        Contact c = new Contact("id-1", "Same", "0712345678", "a@example.com", added, added);
        c.setName("Same");
        assertEquals(added, c.getLastModified());
    }

    @Test
    void equalityIsBasedOnIdOnly() {
        LocalDateTime t = LocalDateTime.of(2020, 1, 1, 10, 0);
        Contact a = new Contact("same-id", "Alice", "0712345678", "a@example.com", t, t);
        Contact b = new Contact("same-id", "Different", "0823456789", "b@example.com", t, t);
        Contact other = new Contact("Alice", "0712345678", "a@example.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, other);   // same details, different generated id
    }

    @Test
    void fullConstructorValidatesIdAndDate() {
        LocalDateTime t = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class,
                () -> new Contact(" ", "A", "0712345678", "a@example.com", t, t));
        assertThrows(NullPointerException.class,
                () -> new Contact("id", "A", "0712345678", "a@example.com", null, t));
    }

    @Test
    void comparatorsOrderCorrectly() {
        LocalDateTime t1 = LocalDateTime.of(2020, 1, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2021, 1, 1, 10, 0);
        Contact bob = new Contact("1", "bob", "0823456789", "b@example.com", t2, t2);
        Contact alice = new Contact("2", "Alice", "0712345678", "a@example.com", t1, t1);

        List<Contact> list = new ArrayList<>(List.of(bob, alice));

        list.sort(Contact.BY_NAME);                 // case-insensitive
        assertEquals("Alice", list.get(0).getName());

        list.sort(Contact.BY_PHONE);
        assertEquals("0712345678", list.get(0).getPhone());

        list.sort(Contact.BY_DATE_ADDED);
        assertEquals(alice, list.get(0));

        list.sort(Contact.BY_DATE_ADDED.reversed());
        assertEquals(bob, list.get(0));
    }

    @Test
    void contactSurvivesSerialisation() throws Exception {
        Contact original = new Contact("Jane Doe", "0845678901", "jane@example.com");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            Contact copy = (Contact) in.readObject();
            assertEquals(original, copy);
            assertEquals("Jane Doe", copy.getName());
            assertEquals(original.getDateAdded(), copy.getDateAdded());
        }
    }
}