
package search;

import model.Contact;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Contactsort
{
   
    public static void selectionSortByName( List<Contact> contacts )
    {
        int n = contacts.size();

        for ( int i = 0; i < n - 1; i++ )
        {
            int smallest = i; // assume current position holds the smallest name

            // search the remaining unsorted part for a smaller name
            for ( int j = i + 1; j < n; j++ )
            {
                if ( contacts.get( j ).getName().compareToIgnoreCase(
                        contacts.get( smallest ).getName() ) < 0 )
                {
                    smallest = j; // remember location of new smallest name
                } 
            } 

           
            if ( smallest != i )
            {
                Contact temp = contacts.get( i );
                contacts.set( i, contacts.get( smallest ) );
                contacts.set( smallest, temp );
            } 
        } 
    } 


    // O(n^2), O(n) best case
    public static void insertionSortByName( List<Contact> contacts )
    {
        // start with the second contact; the first, by itself, is
        // already considered sorted
        for ( int i = 1; i < contacts.size(); i++ )
        {
            Contact insertValue = contacts.get( i ); // contact to insert
            int j = i - 1;

            // shift larger named contacts one position to the right
            while ( j >= 0 && contacts.get( j ).getName()
                        .compareToIgnoreCase( insertValue.getName() ) > 0 )
            {
                contacts.set( j + 1, contacts.get( j ) );
                j--;
            } 

            // insert the value into its correct position
            contacts.set( j + 1, insertValue );
        } 
    } 


    //  O(n log n)
    public static void mergeSortByName( List<Contact> contacts )
    {
        if ( contacts.size() > 1 )
        {
            mergeSort( contacts, 0, contacts.size() - 1 );
        }
    } 

    private static void mergeSort( List<Contact> contacts, int left, int right )
    {
        if ( left < right ) // recursion step,  more than one element
        {
            int middle = ( left + right ) / 2;

            mergeSort( contacts, left, middle );       // sort left half
            mergeSort( contacts, middle + 1, right );  // sort right half
            merge( contacts, left, middle, right );    // merge the two halves
        } 
        // base case: left >= right means zero or one element -- already sorted
    } 

    private static void merge(
        List<Contact> contacts, int left, int middle, int right )
    {
        int n1 = middle - left + 1;
        int n2 = right - middle;

        List<Contact> leftHalf  = new java.util.ArrayList<>( contacts.subList( left, middle + 1 ) );
        List<Contact> rightHalf = new java.util.ArrayList<>( contacts.subList( middle + 1, right + 1 ) );

        int i = 0, j = 0, k = left;

        while ( i < n1 && j < n2 )
        {
            if ( leftHalf.get( i ).getName().compareToIgnoreCase(
                    rightHalf.get( j ).getName() ) <= 0 )
            {
                contacts.set( k, leftHalf.get( i ) );
                i++;
            }
            else
            {
                contacts.set( k, rightHalf.get( j ) );
                j++;
            } 
            k++;
        } 

        while ( i < n1 )
        {
            contacts.set( k, leftHalf.get( i ) );
            i++;
            k++;
        } 

        while ( j < n2 )
        {
            contacts.set( k, rightHalf.get( j ) );
            j++;
            k++;
        } 
    } 
    
    
    public static void sortByName( List<Contact> contacts )
    {
        contacts.sort( Comparator.comparing( Contact::getName, String.CASE_INSENSITIVE_ORDER ) );
    } // end method sortByName

    public static void sortByPhone( List<Contact> contacts )
    {
        contacts.sort( Comparator.comparing( Contact::getPhone ) );
    } 

} 