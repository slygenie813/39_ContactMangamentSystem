package util;

import java.util.regex.Pattern;

public class Validation {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[0-9]{7,15}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String normalized = phone.trim().replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(normalized).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
