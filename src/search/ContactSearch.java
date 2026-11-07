package search;

import model.Contact;
import util.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactSearch
{
    // Starts from index 0 and compares each contact's name to key.
    public static int linearSearchByName( List<Contact> contacts, String key )
    {
        if ( contacts == null || key == null || key.trim().isEmpty() )
        {
            return -1;
        }

        for ( int i = 0; i < contacts.size(); i++ )
        {
            if ( contacts.get( i ).getName().equalsIgnoreCase( key.trim() ) )
            {
                return i;
            }
        }
        return -1;
    }

    // Convenience overload: always starts at index 0.
    // (ContactSearchTest calls recursiveLinearSearchByName(list, key).)
    public static int recursiveLinearSearchByName(
        List<Contact> contacts, String key )
    {
        return recursiveLinearSearchByName( contacts, key, 0 );
    }

    public static int recursiveLinearSearchByName(
        List<Contact> contacts, String key, int index )
    {
        // base case: nothing to search, or the end has been reached
        if ( contacts == null || key == null || key.trim().isEmpty() ||
             index < 0 || index >= contacts.size() )
        {
            return -1;
        }
        // base case: found a match at this index
        if ( contacts.get( index ).getName().equalsIgnoreCase( key.trim() ) )
        {
            return index;
        }
        return recursiveLinearSearchByName( contacts, key, index + 1 );
    }

    // The list MUST already be sorted by name (case-insensitive).
    public static int binarySearchByName( List<Contact> contacts, String key )
    {
        if ( contacts == null || key == null || key.trim().isEmpty() )
        {
            return -1;
        }

        String target = key.trim();
        int low = 0;
        int high = contacts.size() - 1;

        while ( low <= high )
        {
            int middle = ( low + high ) >>> 1;    // avoids int overflow
            int comparison =
                contacts.get( middle ).getName().compareToIgnoreCase( target );

            if ( comparison == 0 )
            {
                return middle;
            }
            else if ( comparison < 0 )
            {
                low = middle + 1;
            }
            else
            {
                high = middle - 1;
            }
        }
        return -1;
    }

    public static List<Contact> searchByName( List<Contact> contacts, String query )
    {
        return searchByName( contacts, query, true );
    }

    public static List<Contact> searchByName(
        List<Contact> contacts, String query, boolean ignoreCase )
    {
        List<Contact> results = new ArrayList<>();
        if ( contacts == null || isBlank( query ) )
        {
            return results;
        }

        String key = prepare( query, ignoreCase );
        for ( Contact c : contacts )
        {
            if ( prepare( c.getName(), ignoreCase ).contains( key ) )
            {
                results.add( c );
            }
        }
        return results;
    }

    // Spaces, brackets and hyphens in the query are ignored, because
    // stored phone numbers are already normalised ("071 234" -> "071234").
    public static List<Contact> searchByPhone( List<Contact> contacts, String query )
    {
        List<Contact> results = new ArrayList<>();
        if ( contacts == null || isBlank( query ) )
        {
            return results;
        }

        String key = Validation.normalizePhone( query );
        if ( key.isEmpty() )          // query was only "( ) -" characters
        {
            return results;
        }

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
        if ( contacts == null || isBlank( query ) )
        {
            return results;
        }

        String key = prepare( query, ignoreCase );
        for ( Contact c : contacts )
        {
            if ( prepare( c.getEmail(), ignoreCase ).contains( key ) )
            {
                results.add( c );
            }
        }
        return results;
    }

    public static List<Contact> search( List<Contact> contacts, String query )
    {
        return search( contacts, query, true );
    }

    public static List<Contact> search(
        List<Contact> contacts, String query, boolean ignoreCase )
    {
        List<Contact> results = new ArrayList<>();
        if ( contacts == null )
        {
            return results;
        }
        if ( isBlank( query ) )
        {
            results.addAll( contacts );
            return results;
        }

        String key = prepare( query, ignoreCase );
        String phoneKey = Validation.normalizePhone( query );

        for ( Contact c : contacts )
        {
            boolean nameHit  = prepare( c.getName(),  ignoreCase ).contains( key );
            boolean emailHit = prepare( c.getEmail(), ignoreCase ).contains( key );
            boolean phoneHit = !phoneKey.isEmpty() && c.getPhone().contains( phoneKey );

            if ( nameHit || emailHit || phoneHit )
            {
                results.add( c );
            }
        }
        return results;
    }
    

    private static boolean isBlank( String s )
    {
        return s == null || s.trim().isEmpty();
    }

    // trims, and lower-cases (locale-independent) when ignoring case
    private static String prepare( String s, boolean ignoreCase )
    {
        String t = s.trim();
        return ignoreCase ? t.toLowerCase( Locale.ROOT ) : t;
    }
}