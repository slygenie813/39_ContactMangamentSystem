package service;

// Thrown when an account cannot be created (bad username, weak password...). 
public class AuthException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}