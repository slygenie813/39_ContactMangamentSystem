

package search;

import model.Contact;
import java.util.ArrayList;
import java.util.List;
import model.Contact;

public class Contactsearchsortdemo
{
    public static void main( String[] args ) throws InterruptedException
    {
        // create a small sample contact list to test against
        List<Contact> contacts = new ArrayList<>();
        contacts.add( new Contact( "Thabo Nkosi",   "0712345678", "thabo@example.com" ) );
        contacts.add( new Contact( "Amahle Dlamini", "0823456789", "amahle@example.com" ) );
        contacts.add( new Contact( "John Smith",    "0834567890", "john@example.com" ) );
        contacts.add( new Contact( "Jane Doe",      "0845678901", "jane@example.com" ) );
        contacts.add( new Contact( "Bongani Zulu",  "0856789012", "bongani@example.com" ) );

        System.out.println( "Unsorted contact list:" );
        printContacts( contacts );

       
        System.out.println( "\n--- Linear Search (exact match) ---" );
        int idx = ContactSearch.linearSearchByName( contacts, "John Smith" );
        System.out.printf( "linearSearchByName(\"John Smith\") -> index %d%n", idx );

       
        System.out.println( "\n--- Recursive Linear Search ---" );
        int recursiveIdx =
            ContactSearch.recursiveLinearSearchByName( contacts, "Jane Doe", 0 );
        System.out.printf( "recursiveLinearSearchByName(\"Jane Doe\") -> index %d%n", recursiveIdx );

        
        System.out.println( "\n--- Substring Search ---" );
        List<Contact> matches = ContactSearch.searchByName( contacts, "an" );
        System.out.println( "searchByName(\"an\") matches:" );
        printContacts( matches );

       
        List<Contact> selectionCopy = new ArrayList<>( contacts );
        Contactsort.selectionSortByName( selectionCopy );
        System.out.println( "\n--- After Selection Sort (by name) ---" );
        printContacts( selectionCopy );

        // ---------- Example: Insertion Sort ----------
        List<Contact> insertionCopy = new ArrayList<>( contacts );
        Contactsort.insertionSortByName( insertionCopy );
        System.out.println( "\n--- After Insertion Sort (by name) ---" );
        printContacts( insertionCopy );

       
        List<Contact> mergeCopy = new ArrayList<>( contacts );
        Contactsort.mergeSortByName( mergeCopy );
        System.out.println( "\n--- After Merge Sort (by name) ---" );
        printContacts( mergeCopy );

        
        List<Contact> sorted = new ArrayList<>( contacts );
        Contactsort.sortByName( sorted );
        System.out.println( "\n--- After ContactSort.sortByName (production) ---" );
        printContacts( sorted );

        System.out.println( "\n--- Binary Search (requires sorted list) ---" );
        int binaryIdx = ContactSearch.binarySearchByName( sorted, "Jane Doe" );
        System.out.printf( "binarySearchByName(\"Jane Doe\") -> index %d%n", binaryIdx );

       
        System.out.println( "\n--- ContactIndex (HashMap exact lookup) ---" );
        Contactindex index = new Contactindex();
        index.rebuild( contacts );
        Contact found = index.findByExactPhone( "0834567890" );
        System.out.println( "findByExactPhone(\"0834567890\") -> "
            + ( found != null ? found.getName() : "not found" ) );

        
        System.out.println( "\n--- Concurrent Search ---" );
        List<Contact> concurrentMatches =
            Concurrentcontactsearch.searchByNameConcurrent( contacts, "an", 2 );
        System.out.println( "searchByNameConcurrent(\"an\", 2 threads) matches:" );
        printContacts( concurrentMatches );

    } 

    // helper method to print a list of contacts
    private static void printContacts( List<Contact> contacts )
    {
        for ( Contact c : contacts )
        {
            System.out.println( c );
        } 
    } 

} 
