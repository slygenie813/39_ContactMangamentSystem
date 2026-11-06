package search;

import model.Contact;
import java.util.ArrayList;
import java.util.List;

public class Concurrentcontactsearch
{
    // Splits contacts into t chunks and  searches each chunk on its
    // own Thread  and merges the results once every thread has finished.
    public static List<Contact> searchByNameConcurrent(List<Contact> contacts, String query, int threadCount )throws InterruptedException{
        
        if ( threadCount < 1 ){
            throw new IllegalArgumentException( "threadCount must be at least 1" );
        }
        
        int n = contacts.size();
        int chunkSize = Math.max( 1, n / threadCount );

        Thread[] threads = new Thread[ threadCount ];

        // each thread gets its OWN results list no shared write target,  
        @SuppressWarnings( "unchecked" )
        List<Contact>[] partialResults = new List[ threadCount ];

        for ( int t = 0; t < threadCount; t++ )
        {
            final int start = t * chunkSize;
            final int end = ( t == threadCount - 1 ) ? n : Math.min( start + chunkSize, n );
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
        } // end for

        // wait for every thread to finish before combining results
        for ( Thread thread : threads )
        {
            thread.join();
        } 

        List<Contact> results = new ArrayList<>();

        for ( List<Contact> partial : partialResults )
        {
            results.addAll( partial );
        } 

        return results;
    } 

} 