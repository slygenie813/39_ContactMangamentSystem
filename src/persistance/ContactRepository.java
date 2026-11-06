/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package persistance;

import model.Contact;
import java.util.List;


public interface ContactRepository {

    /**
     * Loads every saved contact. Returns an empty list when nothing has
     * been saved yet.
     *
     * @throws PersistenceException if saved data exists but cannot be read
     */
    List<Contact> load();

    /**
     * Saves the complete list, replacing what was stored before.
     *
     * @throws PersistenceException if the list could not be saved; the
     *                              previously stored data must stay intact
     */
    void save(List<Contact> contacts);
}