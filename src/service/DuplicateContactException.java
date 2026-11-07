
package service;

public class DuplicateContactException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateContactException(String message) {
        super(message);
    }
}
