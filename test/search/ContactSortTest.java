package search;

import model.Contact;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ContactSortTest {

    private static List<Contact> unsorted() {
        List<Contact> l = new ArrayList<>();
        l.add(new Contact("Thabo Nkosi",    "0856789012", "thabo@example.com"));
        l.add(new Contact("Jane Doe",       "0712345678", "jane@example.com"));
        l.add(new Contact("Bongani Zulu",   "0845678901", "bongani@example.com"));
        l.add(new Contact("Amahle Dlamini", "0823456789", "amahle@example.com"));
        l.add(new Contact("John Smith",     "0834567890", "john@example.com"));
        return l;
    }

    private static List<String> names(List<Contact> list) {
        List<String> n = new ArrayList<>();
        for (Contact c : list) n.add(c.getName());
        return n;
    }

    private static final List<String> EXPECTED = List.of(
            "Amahle Dlamini", "Bongani Zulu", "Jane Doe", "John Smith", "Thabo Nkosi");

    private static void check(Consumer<List<Contact>> sorter) {
        List<Contact> list = unsorted();
        sorter.accept(list);
        assertEquals(EXPECTED, names(list));
    }

    @Test void selectionSortSortsByName() { check(Contactsort::selectionSortByName); }
    @Test void insertionSortSortsByName() { check(Contactsort::insertionSortByName); }
    @Test void mergeSortSortsByName()     { check(Contactsort::mergeSortByName); }
    @Test void builtInSortSortsByName()   { check(Contactsort::sortByName); }

    @Test
    void sortByPhoneIsAscending() {
        List<Contact> list = unsorted();
        Contactsort.sortByPhone(list);
        assertEquals("0712345678", list.get(0).getPhone());
        assertEquals("0856789012", list.get(4).getPhone());
    }

    @Test
    void allSortsHandleEmptyAndSingleLists() {
        List<Consumer<List<Contact>>> sorters = List.of(
                Contactsort::selectionSortByName, Contactsort::insertionSortByName,
                Contactsort::mergeSortByName, Contactsort::sortByName);
        for (Consumer<List<Contact>> s : sorters) {
            List<Contact> empty = new ArrayList<>();
            s.accept(empty);
            assertTrue(empty.isEmpty());

            List<Contact> one = new ArrayList<>();
            one.add(new Contact("Solo", "0712345678", "solo@example.com"));
            s.accept(one);
            assertEquals(1, one.size());
        }
    }

    @Test
    void allSortsHandleAlreadySortedAndReversedInput() {
        List<Contact> sorted = unsorted();
        Contactsort.sortByName(sorted);
        List<Contact> reversed = new ArrayList<>(sorted);
        java.util.Collections.reverse(reversed);

        for (Consumer<List<Contact>> s : List.<Consumer<List<Contact>>>of(
                Contactsort::selectionSortByName, Contactsort::insertionSortByName,
                Contactsort::mergeSortByName)) {
            List<Contact> a = new ArrayList<>(sorted);
            s.accept(a);
            assertEquals(EXPECTED, names(a));

            List<Contact> b = new ArrayList<>(reversed);
            s.accept(b);
            assertEquals(EXPECTED, names(b));
        }
    }

    @Test
    void sortingIgnoresCase() {
        List<Contact> list = new ArrayList<>();
        list.add(new Contact("carl",  "0712345678", "c@example.com"));
        list.add(new Contact("Alice", "0823456789", "a@example.com"));
        list.add(new Contact("bob",   "0834567890", "b@example.com"));
        Contactsort.mergeSortByName(list);
        assertEquals(List.of("Alice", "bob", "carl"), names(list));
    }

    @Test
    void allAlgorithmsAgreeOnLargeRandomList() {
        Random rnd = new Random(7);
        List<Contact> data = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            data.add(new Contact("Name" + rnd.nextInt(10_000), "0712345678", "x" + i + "@example.com"));
        }

        List<Contact> reference = new ArrayList<>(data);
        Contactsort.sortByName(reference);

        List<Contact> sel = new ArrayList<>(data);
        List<Contact> ins = new ArrayList<>(data);
        List<Contact> mrg = new ArrayList<>(data);
        Contactsort.selectionSortByName(sel);
        Contactsort.insertionSortByName(ins);
        Contactsort.mergeSortByName(mrg);

        assertEquals(names(reference), names(sel));
        assertEquals(names(reference), names(ins));
        assertEquals(names(reference), names(mrg));
        assertEquals(data.size(), mrg.size());   // nothing lost or duplicated
    }
}