package search;

import model.Contact;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactSearchTest {

    private static List<Contact> sample() {
        List<Contact> l = new ArrayList<>();
        l.add(new Contact("Thabo Nkosi",    "0712345678", "thabo@example.com"));
        l.add(new Contact("Amahle Dlamini", "0823456789", "amahle@example.com"));
        l.add(new Contact("John Smith",     "0834567890", "john@work.org"));
        l.add(new Contact("Jane Doe",       "0845678901", "jane@example.com"));
        l.add(new Contact("Bongani Zulu",   "0856789012", "bongani@example.com"));
        return l;
    }

    // linear search 

    @Test
    void linearSearchFindsExactNameIgnoringCase() {
        assertEquals(2, ContactSearch.linearSearchByName(sample(), "john smith"));
        assertEquals(0, ContactSearch.linearSearchByName(sample(), "  Thabo Nkosi  "));
    }

    @Test
    void linearSearchReturnsMinusOneWhenMissingOrInvalid() {
        assertEquals(-1, ContactSearch.linearSearchByName(sample(), "Nobody"));
        assertEquals(-1, ContactSearch.linearSearchByName(sample(), null));
        assertEquals(-1, ContactSearch.linearSearchByName(new ArrayList<>(), "Jane Doe"));
    }

    //  recursive linear search 

    @Test
    void recursiveSearchGivesSameAnswerAsLinear() {
        List<Contact> list = sample();
        for (Contact c : list) {
            assertEquals(ContactSearch.linearSearchByName(list, c.getName()),
                    ContactSearch.recursiveLinearSearchByName(list, c.getName()));
        }
    }

    @Test
    void recursiveSearchBaseCases() {
        assertEquals(-1, ContactSearch.recursiveLinearSearchByName(sample(), "Nobody"));
        assertEquals(-1, ContactSearch.recursiveLinearSearchByName(new ArrayList<>(), "Jane Doe"));
        assertEquals(-1, ContactSearch.recursiveLinearSearchByName(sample(), null));
        assertEquals(3, ContactSearch.recursiveLinearSearchByName(sample(), "JANE DOE", 0));
    }

    // binary search 

    @Test
    void binarySearchFindsEveryNameInASortedList() {
        List<Contact> sorted = sample();
        Contactsort.sortByName(sorted);
        for (int i = 0; i < sorted.size(); i++) {
            assertEquals(i, ContactSearch.binarySearchByName(sorted, sorted.get(i).getName()));
        }
    }

    @Test
    void binarySearchEdgeCases() {
        List<Contact> sorted = sample();
        Contactsort.sortByName(sorted);
        assertEquals(-1, ContactSearch.binarySearchByName(sorted, "Nobody"));
        assertEquals(-1, ContactSearch.binarySearchByName(sorted, "Aaron"));   // before first
        assertEquals(-1, ContactSearch.binarySearchByName(sorted, "Zzzz"));    // after last
        assertEquals(-1, ContactSearch.binarySearchByName(sorted, null));
        assertEquals(-1, ContactSearch.binarySearchByName(new ArrayList<>(), "Jane Doe"));

        List<Contact> single = new ArrayList<>();
        single.add(new Contact("Only One", "0712345678", "one@example.com"));
        assertEquals(0, ContactSearch.binarySearchByName(single, "only one"));
    }

    // substring searches 

    @Test
    void searchByNamePartialAndKeepsOrder() {
        List<Contact> r = ContactSearch.searchByName(sample(), "an");
        assertEquals(2, r.size());
        assertEquals("Jane Doe", r.get(0).getName());
        assertEquals("Bongani Zulu", r.get(1).getName());
    }

    @Test
    void searchByNameCaseOption() {
        assertEquals(1, ContactSearch.searchByName(sample(), "JANE").size());
        assertEquals(1, ContactSearch.searchByName(sample(), "JANE", true).size());
        assertEquals(0, ContactSearch.searchByName(sample(), "JANE", false).size());
    }

    @Test
    void searchByPhoneIgnoresSpacesInQuery() {
        List<Contact> r = ContactSearch.searchByPhone(sample(), "071 234");
        assertEquals(1, r.size());
        assertEquals("Thabo Nkosi", r.get(0).getName());
        assertEquals(1, ContactSearch.searchByPhone(sample(), "0834").size());
        assertEquals(0, ContactSearch.searchByPhone(sample(), "9999").size());
    }

    @Test
    void searchByEmail() {
        assertEquals(1, ContactSearch.searchByEmail(sample(), "work.org").size());
        assertEquals(4, ContactSearch.searchByEmail(sample(), "EXAMPLE.COM").size());
        assertEquals(0, ContactSearch.searchByEmail(sample(), "EXAMPLE.COM", false).size());
    }

    // combined search 

    @Test
    void combinedSearchMatchesNamePhoneOrEmail() {
        assertEquals(1, ContactSearch.search(sample(), "smith").size());     // name
        assertEquals(1, ContactSearch.search(sample(), "0856").size());      // phone
        assertEquals(4, ContactSearch.search(sample(), "example.com").size()); // email
    }

    @Test
    void combinedSearchNoMatchAndEmptyList() {
        assertTrue(ContactSearch.search(sample(), "zzzzz").isEmpty());
        assertTrue(ContactSearch.search(new ArrayList<>(), "jane").isEmpty());
    }

    @Test
    void emptyOrNullQueryReturnsEverything() {
        assertEquals(5, ContactSearch.search(sample(), "").size());
        assertEquals(5, ContactSearch.search(sample(), null).size());
    }
}