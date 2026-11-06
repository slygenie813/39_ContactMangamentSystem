package search;

import model.Contact;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceBenchmarkTest {

    private static List<Contact> build(int n) {
        Random r = new Random(42);
        List<Contact> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Contact("Person " + r.nextInt(1_000_000) + " " + i,
                    String.format("08%08d", i), "user" + i + "@example.com"));
        }
        return list;
    }

    private static double ms(Runnable r) {
        long t = System.nanoTime();
        r.run();
        return (System.nanoTime() - t) / 1e6;
    }

    private static List<String> names(List<Contact> list) {
        List<String> n = new ArrayList<>();
        for (Contact c : list) n.add(c.getName());
        return n;
    }

    @Test
    void benchmarkSortsAndSearches() {
        System.out.println();
        System.out.printf("%-8s %-10s %-10s %-10s %-10s | %-12s %-12s %-12s %-12s%n",
                "n", "selection", "insertion", "merge", "built-in",
                "linear(us)", "binary(us)", "phone scan", "phone index");

        for (int n : new int[]{10, 1000, 5000}) {
            List<Contact> data = build(n);

            List<Contact> a = new ArrayList<>(data);
            List<Contact> b = new ArrayList<>(data);
            List<Contact> c = new ArrayList<>(data);
            List<Contact> d = new ArrayList<>(data);
            double sel = ms(() -> Contactsort.selectionSortByName(a));
            double ins = ms(() -> Contactsort.insertionSortByName(b));
            double mrg = ms(() -> Contactsort.mergeSortByName(c));
            double bui = ms(() -> Contactsort.sortByName(d));

            // all four algorithms must agree
            assertEquals(names(d), names(a));
            assertEquals(names(d), names(b));
            assertEquals(names(d), names(c));

            // search for the last contact of the ORIGINAL (unsorted) list
            Contact target = data.get(n - 1);
            final int reps = 1000;

            double lin = ms(() -> {
                for (int i = 0; i < reps; i++) ContactSearch.linearSearchByName(data, target.getName());
            }) * 1000 / reps;

            double bin = ms(() -> {
                for (int i = 0; i < reps; i++) ContactSearch.binarySearchByName(d, target.getName());
            }) * 1000 / reps;

            int li = ContactSearch.linearSearchByName(data, target.getName());
            int bi = ContactSearch.binarySearchByName(d, target.getName());
            assertEquals(target, data.get(li));
            assertEquals(target, d.get(bi));

            // phone: linear scan vs HashMap index
            Contactindex idx = new Contactindex();
            idx.rebuild(data);
            double scan = ms(() -> {
                for (int i = 0; i < reps; i++) {
                    for (Contact x : data) {
                        if (x.getPhone().equals(target.getPhone())) break;
                    }
                }
            }) * 1000 / reps;
            double hash = ms(() -> {
                for (int i = 0; i < reps; i++) idx.findByExactPhone(target.getPhone());
            }) * 1000 / reps;
            assertEquals(target, idx.findByExactPhone(target.getPhone()));

            System.out.printf("%-8d %-10.2f %-10.2f %-10.2f %-10.2f | %-12.2f %-12.2f %-12.2f %-12.2f%n",
                    n, sel, ins, mrg, bui, lin, bin, scan, hash);
        }
        System.out.println("(sort columns in ms; search columns are microseconds per lookup)");
    }
}