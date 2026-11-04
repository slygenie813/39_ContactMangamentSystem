package model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;


public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{7,15}$");

    // Contact information
    private final String id;
    private final LocalDateTime dateAdded;

    private String name;
    private String phoneNumber;
    private String email;
    private LocalDateTime lastModified;

    /**
     * Creates a new contact.
     */
    public Contact(String name, String phoneNumber, String email) {

        this.id = UUID.randomUUID().toString();
        this.dateAdded = LocalDateTime.now();

        this.name = validateName(name);
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.email = validateEmail(email);

        this.lastModified = this.dateAdded;
    }

    /**
     * Constructor used when loading an existing contact
     * from a file.
     */
    public Contact(String id, String name, String phoneNumber,
                   String email, LocalDateTime dateAdded,
                   LocalDateTime lastModified) {

        this.id = validateId(id);
        this.name = validateName(name);
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.email = validateEmail(email);

        this.dateAdded = Objects.requireNonNull(
                dateAdded,
                "Date added cannot be null."
        );

        this.lastModified = lastModified != null
                ? lastModified
                : dateAdded;
    }

    // --------------------------------------------------
    // Validation methods
    // --------------------------------------------------

    private static String validateId(String id) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Contact ID cannot be empty."
            );
        }

        return id.trim();
    }

    private static String validateName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }

        return name.trim();
    }

    private static String validatePhoneNumber(String phoneNumber) {

        if (phoneNumber == null) {
            throw new IllegalArgumentException(
                    "Phone number cannot be null."
            );
        }

        // Removes spaces, brackets and hyphens
        String normalized = phoneNumber.trim()
                .replaceAll("[\\s()-]", "");

        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number. Enter 7-15 digits "
                    + "with an optional + at the beginning."
            );
        }

        return normalized;
    }

    private static String validateEmail(String email) {

        if (email == null) {
            throw new IllegalArgumentException(
                    "Email cannot be null."
            );
        }

        String normalized = email.trim()
                .toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email address."
            );
        }

        return normalized;
    }

    // --------------------------------------------------
    // Getters
    // --------------------------------------------------

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    // --------------------------------------------------
    // Setters
    // --------------------------------------------------

    public void setName(String name) {

        String validatedName = validateName(name);

        if (!this.name.equals(validatedName)) {
            this.name = validatedName;
            updateLastModified();
        }
    }

    public void setPhoneNumber(String phoneNumber) {

        String validatedPhone = validatePhoneNumber(phoneNumber);

        if (!this.phoneNumber.equals(validatedPhone)) {
            this.phoneNumber = validatedPhone;
            updateLastModified();
        }
    }

    public void setEmail(String email) {

        String validatedEmail = validateEmail(email);

        if (!this.email.equals(validatedEmail)) {
            this.email = validatedEmail;
            updateLastModified();
        }
    }

    /**
     * Updates the last modified date.
     */
    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    // --------------------------------------------------
    // Contact methods
    // --------------------------------------------------

    /**
     * Checks whether two contacts have the same ID.
     */
    public boolean hasSameId(Contact other) {

        if (other == null) {
            return false;
        }

        return this.id.equals(other.id);
    }

    /**
     * Returns a hash value based on the contact ID.
     */
    public int getContactHashCode() {
        return Objects.hash(id);
    }

    /**
     * Compares two contacts by name.
     *
     * Returns:
     * negative number if this contact comes first
     * zero if the names are equal
     * positive number if the other contact comes first
     */
    public int compareNames(Contact other) {

        if (other == null) {
            throw new IllegalArgumentException(
                    "Contact cannot be null."
            );
        }

        return this.name.compareToIgnoreCase(other.name);
    }

    /**
     * Returns the contact information as a String.
     */
    public String getContactDetails() {

        return "ID: " + id
                + "\nName: " + name
                + "\nPhone: " + phoneNumber
                + "\nEmail: " + email;
    }
}
