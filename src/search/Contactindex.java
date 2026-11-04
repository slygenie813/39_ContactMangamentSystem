

package search;

import model.Contact;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contactindex
{
    private final Map<String, Contact> phoneIndex = new HashMap<>();

    // Build (or rebuild) the index from the current contact list.
    // O(n) -- must insert every contact once. Call this once after load.
    public void rebuild( List<Contact> contacts )
    {
        phoneIndex.clear();

        for ( Contact c : contacts )
        {
            phoneIndex.put( c.getPhone(), c ); // duplicate phone numbers overwrite, same as the map.put("Alice", 28) example
        } 
    } 

    // O(1) average case -- exact phone number lookup only.
    public Contact findByExactPhone( String phone )
    {
        return phoneIndex.get( phone );
    } 

    // Call from ContactManager.addContact() so the index stays in sync.
    public void onAdd( Contact c )
    {
        phoneIndex.put( c.getPhone(), c ); // O(1)
    } 

    // Call from ContactManager.deleteContact() so the index stays in sync.
    public void onRemove( Contact c )
    {
        phoneIndex.remove( c.getPhone() ); // O(1)
    } 

    public int size()
    {
        return phoneIndex.size();
    } 

} 