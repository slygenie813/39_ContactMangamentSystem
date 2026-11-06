package search;

import model.Contact;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentSearchTest {

    private static List<Contact> build(int n) {
        List<Contact> l = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = (i % 3 == 0 ? "Anna " : "Peter ") + i;
            l.add(new Contact(name, String.format("08%08d", i), "user" + i + "@example.com"));
        }
        return l;
    }

    @Test
    void matchesSequentialSearchForDifferentThreadCounts() throws Exception {
        List<Contact> data = build(100);
        List<Contact> expected = ContactSearch.searchByName(data, "anna");

        for (int threads : new int[]{1, 2, 3, 4, 8}) {
            List<Contact> actual = Concurrentcontactsearch.searchByNameConcurrent(data, "anna", threads);
            assertEquals(expected, actual, threads + " threads");
        }
    }

    @Test
    void moreThreadsThanContactsStillWorks() throws Exception {
        List<Contact> data = build(3);
        List<Contact> actual = Concurrentcontactsearch.searchByNameConcurrent(data, "anna", 8);
        assertEquals(ContactSearch.searchByName(data, "anna"), actual);
    }

    @Test
    void emptyListAndNoMatch() throws Exception {
        assertTrue(Concurrentcontactsearch
                .searchByNameConcurrent(new ArrayList<>(), "anna", 4).isEmpty());
        assertTrue(Concurrentcontactsearch
                .searchByNameConcurrent(build(50), "zzzzz", 4).isEmpty());
    }
}