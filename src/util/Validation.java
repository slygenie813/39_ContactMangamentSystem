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
        return PHONE_PATTERN.matcher(normalizePhone(phone)).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    
     // Strips spaces, brackets and hyphens so "071 234-5678" and
     // "0712345678" compare as equal. A null phone becomes "".
     // Used when storing, searching and indexing phone numbers.
     
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.trim().replaceAll("[\\s()-]", "");
    }
}