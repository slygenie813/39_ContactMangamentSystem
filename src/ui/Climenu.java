
package ui;

import java.util.Scanner;



public class Climenu {
    
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        System.out.print("\n _________________________________________________");
        System.out.print("\n|                                                 |");
        System.out.println("\n|_____WELCOME TO CONTACT MANAGEMENT SYSTEM________|");
        System.out.print("*================== CLI MENU ====================*\n");
        System.out.print("* 1. ADD                                 2. EDIT  *\n");
        System.out.print("* 3. DELETE                              4. EXIT  *\n");
        System.out.print("***************************************************\n");
        
System.out.print("Enter your choice: ");
String choice = input.nextLine();
switch(choice){
    case "1": System.out.println("Welcome ADD your contact: ");
    
             break;
    case "2": System.out.println("Welcome EDIT the contact: ");
    
            break;
    case "3": System.out.println("Are you sure you want to delete this contact?[yes/No]");
            break;
    case "4" : System.out.println("Thank for using Contact Management, Bye.");
            break;
        
    default: 
        System.out.println("INVALID INPUT, TRY AGAIN");
 
   
}
    }
    
}