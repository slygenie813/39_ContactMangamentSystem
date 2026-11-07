

package search;

import model.Contact;
import java.util.ArrayList;
import java.util.List;

// Performs a search over the contact list in parallel by splitting the work
// across multiple threads, then combining their results.
public class Concurrentcontactsearch
{
    // Splits contacts into t chunks and  searches each chunk on its
    // own Thread  and merges the results once every thread has finished.
    // threadCount = number of worker threads (and chunks).
    // Caller must handle InterruptedException; the method blocks until
    // all threads finish. Chunk boundaries are computed so the last thread
    // takes any remainder, and every contact is covered exactly once.
    public static List<Contact> searchByNameConcurrent(
            
        List<Contact> contacts, String query, int threadCount )
        throws InterruptedException
    {
        int n = contacts.size();
        
        // Chunk size for the first threadCount-1 threads (at least 1 so we
        // never divide into zero-sized chunks when n < threadCount).
        int chunkSize = Math.max( 1, n / threadCount );
        

        Thread[] threads = new Thread[ threadCount ];
        

        // each thread gets its OWN results list no shared write target, 
        // This avoids synchronization: since every thread writes to a
        // distinct slot, no locks are needed on the results.
        
        @SuppressWarnings( "unchecked" )
        List<Contact>[] partialResults = new List[ threadCount ];

        for ( int t = 0; t < threadCount; t++ )
        {
            // Capture loop variables as final for use in the lambda.
            // Last thread takes [start, n) so no contacts are skipped.
            final int start = t * chunkSize;
            final int end = ( t == threadCount - 1 ) ? n : Math.min( start + chunkSize, n );
            final int threadIndex = t;

            partialResults[ t ] = new ArrayList<>();
            
            threads[ t ] = new Thread( () ->
            {  
                // Guard against empty chunks (possible when threadCount > n)
                if ( start < end )
                {
                    
                    // subList is a view, not a copy -- cheap, and safe here
                    // because the underlying list is only read, never modified.
                    List<Contact> chunk = contacts.subList( start, end );
                    partialResults[ threadIndex ].addAll(
                        ContactSearch.searchByName( chunk, query ) );
                } 
            } );

            threads[ t ].start();
        } // end for

        // wait for every thread to finish before combining results
        // join() blocks the calling thread until each worker terminates,
        // guaranteeing all partialResults are fully populated below.
        for ( Thread thread : threads )
        {
            thread.join();
        } 
         // Merge the per-thread results into one list.
        // Order: results are grouped by chunk (thread order), not by the
        // original contact order across chunks.
        List<Contact> results = new ArrayList<>();

        for ( List<Contact> partial : partialResults )
        {
            results.addAll( partial );
        } 

        return results;
    } 

} 