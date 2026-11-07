package search;

import model.Contact;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Searches the contact list in parallel by splitting it into chunks,
// searching each chunk on its own thread, then joining the results.
public class Concurrentcontactsearch
{
    // threadCount = number of worker threads (and chunks); must be >= 1.
    // Blocks until every thread has finished. The last thread takes any
    // remainder, so every contact is covered exactly once.
    // Results come back in the ORIGINAL list order, because the chunks
    // are consecutive and are merged in chunk order.
    public static List<Contact> searchByNameConcurrent(
        List<Contact> contacts, String query, int threadCount )
        throws InterruptedException
    {
        Objects.requireNonNull( contacts, "contacts cannot be null" );
        if ( threadCount < 1 )
        {
            throw new IllegalArgumentException(
                "threadCount must be at least 1 but was " + threadCount );
        }

        int n = contacts.size();
        int chunkSize = Math.max( 1, n / threadCount );

        Thread[] threads = new Thread[ threadCount ];

        @SuppressWarnings( "unchecked" )
        List<Contact>[] partialResults = new List[ threadCount ];

        for ( int t = 0; t < threadCount; t++ )
        {
            // start can pass n when threadCount > n; clamp it so subList is valid
            final int start = Math.min( t * chunkSize, n );
            final int end = ( t == threadCount - 1 )
                ? n : Math.min( start + chunkSize, n );
            final int threadIndex = t;

            partialResults[ t ] = new ArrayList<>();

            threads[ t ] = new Thread( () ->
            {
                if ( start < end )
                {
                    List<Contact> chunk = contacts.subList( start, end );
                    partialResults[ threadIndex ].addAll(
                        ContactSearch.searchByName( chunk, query ) );
                }
            } );
            threads[ t ].start();
        }

        for ( Thread thread : threads )
        {
            thread.join();   // also makes the workers' writes visible here
        }

        List<Contact> results = new ArrayList<>();
        for ( List<Contact> partial : partialResults )
        {
            results.addAll( partial );
        }
        return results;
    }
}