/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author nhlan
 */
public class DuplicateContactException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateContactException(String message) {
        super(message);
    }
}
