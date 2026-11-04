package search;

import model.Contact;
import util.Validation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fast exact lookup of a contact by phone number (HashMap, O(1) average).
// Phone numbers are normalized, so "071 234 5678" finds "0712345678".
// ContactManager keeps this index in sync when contacts are added or
// removed. Methods are synchronized so the index can be shared safely.
public class Contactindex
{
    private final Map<String, Contact> phoneIndex = new HashMap<>();

    // Build (or rebuild) the index from the current contact list.
    // O(n) -- inserts every contact once. If two contacts share a phone
    // number, the later one in the list wins.
    public synchronized void rebuild( List<Contact> contacts )
    {
        phoneIndex.clear();

        for ( Contact c : contacts )
        {
            phoneIndex.put( Validation.normalizePhone( c.getPhone() ), c );
        }
    }

    // O(1) average case -- exact phone number lookup only.
    public synchronized Contact findByExactPhone( String phone )
    {
        return phoneIndex.get( Validation.normalizePhone( phone ) );
    }

    public synchronized void onAdd( Contact c )
    {
        phoneIndex.put( Validation.normalizePhone( c.getPhone() ), c ); // O(1)
    }

    // Only removes the entry if it really belongs to this contact, so
    // removing one contact can never drop another contact's entry.
    public synchronized void onRemove( Contact c )
    {
        String key = Validation.normalizePhone( c.getPhone() );
        Contact current = phoneIndex.get( key );

        if ( current != null && current.getId().equals( c.getId() ) )
        {
            phoneIndex.remove( key );
        }
    }

    public synchronized int size()
    {
        return phoneIndex.size();
    }

}