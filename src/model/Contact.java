

package model;

public class Contact
{
    private String name;   //contact's full name
    private String phone;  //contact's phone number
    private String email;  //contact email address

    // constructor initialises name, phone and email
    public Contact( String name, String phone, String email )
    {
        this.name = name;
        this.phone = phone;
        this.email = email;
    } 

    
    public void setName( String name )
    {
        this.name = name; // store the name
    } 

    // method to get the name
    public String getName()
    {
        return name;
    } 

    // method to set the phone number
    public void setPhone( String phone )
    {
        this.phone = phone; // store the phone number
    } 

    //method to get phone number
    public String getPhone()
    {
        return phone;
    } 

    // method to set the email address
    public void setEmail( String email )
    {
        this.email = email; // store the email address
    } 

    // method to get the email address
    public String getEmail()
    {
        return email;
    } 

    
    @Override
    public String toString()
    {
        return String.format( "%-20s %-15s %s", name, phone, email );
    } 

} //