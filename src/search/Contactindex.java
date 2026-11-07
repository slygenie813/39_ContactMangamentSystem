


package search;

import model.Contact;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contactindex
{
    private final Map<String, Contact> phoneIndex = new HashMap<>();

    // Build (or rebuild) the index from the current contact list.
    // O(n) -- must insert every contact once. Call this after loading.
    public void rebuild( List<Contact> contacts )
    {
        phoneIndex.clear();

        if ( contacts == null )
        {
            return;
        }

        for ( Contact c : contacts )
        {
            phoneIndex.put( c.getPhone(), c );
        } 
    } 

    // O(1) average case -- exact phone number lookup only.
    public Contact findByExactPhone( String phone )
    {
        if ( phone == null || phone.trim().isEmpty() )
        {
            return null;
        }

        String key = phone.trim().replaceAll( "[\\s()-]", "" );

        return phoneIndex.get( key );
    } 

    // Call when a contact is added.
    public void onAdd( Contact c )
    {
        if ( c != null )
        {
            phoneIndex.put( c.getPhone(), c );
        }
    } 

    // Call when a contact is deleted.
    public void onRemove( Contact c )
    {
        if ( c != null )
        {
            phoneIndex.remove( c.getPhone(), c );
        }
    } 

    public int size()
    {
        return phoneIndex.size();
    } 

}
