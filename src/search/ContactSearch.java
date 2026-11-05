package search;

import model.Contact;
import util.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Search algorithms over a list of contacts.
 *
 * A blank (or null) query in the substring searches matches every
 * contact, so a search box that is cleared simply shows everything.
 * The substring searches ignore upper/lower case unless told otherwise.
 */
public class ContactSearch
{
    // ---------- exact-name searches (return an index, or -1) ----------

    // Starts from index 0 and compares each contact's name to key.
    // Time complexity: O(n).
    public static int linearSearchByName( List<Contact> contacts, String key )
    {
        if ( key == null )
        {
            return -1;
        }

        String target = key.trim();

        for ( int i = 0; i < contacts.size(); i++ )
        {
            if ( contacts.get( i ).getName().equalsIgnoreCase( target ) )
            {
                return i; // return index when the value is found
            }
        }

        return -1; // -1 means the value was not found
    }

    // Same result as linearSearchByName, written recursively.
    // Call it with index 0 (or use the two-argument version below).
    // Time O(n); every call adds a stack frame, so the stack depth is O(n)
    // too. For very large lists prefer linearSearchByName, which cannot
    // overflow the stack.
    public static int recursiveLinearSearchByName(
        List<Contact> contacts, String key, int index )
    {
        // base case: key is missing, or we ran past the end of the list
        // (this also covers an empty list)
        if ( key == null || index < 0 || index >= contacts.size() )
        {
            return -1;
        }

        // base case: found the match at this index
        if ( contacts.get( index ).getName().equalsIgnoreCase( key.trim() ) )
        {
            return index;
        }

        // recursive case: check the next index
        return recursiveLinearSearchByName( contacts, key, index + 1 );
    }

    public static int recursiveLinearSearchByName(
        List<Contact> contacts, String key )
    {
        return recursiveLinearSearchByName( contacts, key, 0 );
    }

    // Binary search. The list MUST already be sorted by name
    // (Contactsort.sortByName); on an unsorted list the result is wrong.
    // Time complexity: O(log n). Returns the index of one matching
    // contact, or -1. If several contacts share the name, any of them
    // may be returned.
    public static int binarySearchByName( List<Contact> contacts, String key )
    {
        if ( key == null )
        {
            return -1;
        }

        String target = key.trim();
        int low = 0;
        int high = contacts.size() - 1;

        while ( low <= high )
        {
            int middle = ( low + high ) >>> 1; // cannot overflow

            int comparison = String.CASE_INSENSITIVE_ORDER.compare(
                contacts.get( middle ).getName(), target );

            if ( comparison == 0 )
            {
                return middle; // key found
            }
            else if ( comparison < 0 )
            {
                low = middle + 1; // middle name sorts before key: go right
            }
            else
            {
                high = middle - 1; // middle name sorts after key: go left
            }
        }

        return -1; // not found
    }

    // ---------- substring searches (return the matching contacts) ----------
    // Time complexity of each: O(n * m) for n contacts and a query of
    // length m -- every contact is inspected once.

    public static List<Contact> searchByName( List<Contact> contacts, String query )
    {
        return searchByName( contacts, query, true );
    }

    public static List<Contact> searchByName(
        List<Contact> contacts, String query, boolean ignoreCase )
    {
        List<Contact> results = new ArrayList<>();
        String key = prepare( query );

        for ( Contact c : contacts )
        {
            if ( contains( c.getName(), key, ignoreCase ) )
            {
                results.add( c );
            }
        }

        return results;
    }

    // The query is normalized the same way stored phone numbers are, so
    // "071 234" finds "0712345678".
    public static List<Contact> searchByPhone( List<Contact> contacts, String query )
    {
        List<Contact> results = new ArrayList<>();
        String key = Validation.normalizePhone( query );

        for ( Contact c : contacts )
        {
            if ( c.getPhone().contains( key ) )
            {
                results.add( c );
            }
        }

        return results;
    }

    public static List<Contact> searchByEmail( List<Contact> contacts, String query )
    {
        return searchByEmail( contacts, query, true );
    }

    public static List<Contact> searchByEmail(
        List<Contact> contacts, String query, boolean ignoreCase )
    {
        List<Contact> results = new ArrayList<>();
        String key = prepare( query );

        for ( Contact c : contacts )
        {
            if ( contains( c.getEmail(), key, ignoreCase ) )
            {
                results.add( c );
            }
        }

        return results;
    }

    // One search box for everything: a contact matches if the query is
    // found in its name, its phone number or its email. Results keep the
    // order of the input list, and each contact appears at most once.
    public static List<Contact> search( List<Contact> contacts, String query )
    {
        return search( contacts, query, true );
    }

    public static List<Contact> search(
        List<Contact> contacts, String query, boolean ignoreCase )
    {
        List<Contact> results = new ArrayList<>();
        String text = prepare( query );
        String digits = Validation.normalizePhone( query );

        for ( Contact c : contacts )
        {
            boolean matches =
                contains( c.getName(), text, ignoreCase )
                || contains( c.getEmail(), text, ignoreCase )
                || ( !digits.isEmpty() && c.getPhone().contains( digits ) );

            if ( matches )
            {
                results.add( c );
            }
        }

        return results;
    }

    // ---------- helpers ----------

    private static String prepare( String query )
    {
        return query == null ? "" : query.trim();
    }

    // lower-cases with a fixed locale (so results do not change with the
    // computer's language settings) when ignoring case
    private static boolean contains( String text, String key, boolean ignoreCase )
    {
        if ( !ignoreCase )
        {
            return text.contains( key );
        }

        return text.toLowerCase( Locale.ROOT ).contains( key.toLowerCase( Locale.ROOT ) );
    }

}