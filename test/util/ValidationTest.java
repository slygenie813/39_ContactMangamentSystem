package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void validEmails() {
        assertTrue(Validation.isValidEmail("user@example.com"));
        assertTrue(Validation.isValidEmail("first.last+tag@sub.example.co.za"));
        assertTrue(Validation.isValidEmail("  padded@example.com  "));
    }

    @Test
    void invalidEmails() {
        assertFalse(Validation.isValidEmail(null));
        assertFalse(Validation.isValidEmail(""));
        assertFalse(Validation.isValidEmail("plain"));
        assertFalse(Validation.isValidEmail("a@b"));
        assertFalse(Validation.isValidEmail("a@@example.com"));
        assertFalse(Validation.isValidEmail("a b@example.com"));
        assertFalse(Validation.isValidEmail("user@example."));
    }

    @Test
    void validPhones() {
        assertTrue(Validation.isValidPhone("0712345678"));
        assertTrue(Validation.isValidPhone("071 234-5678"));
        assertTrue(Validation.isValidPhone("(021) 123 4567"));
        assertTrue(Validation.isValidPhone("+27712345678"));
        assertTrue(Validation.isValidPhone("1234567"));              // minimum 7 digits
        assertTrue(Validation.isValidPhone("123456789012345"));      // maximum 15 digits
    }

    @Test
    void invalidPhones() {
        assertFalse(Validation.isValidPhone(null));
        assertFalse(Validation.isValidPhone(""));
        assertFalse(Validation.isValidPhone("123456"));              // too short
        assertFalse(Validation.isValidPhone("1234567890123456"));    // too long
        assertFalse(Validation.isValidPhone("abc1234567"));
        assertFalse(Validation.isValidPhone("07123+45678"));         // + only allowed first
    }

    @Test
    void normalisePhoneStripsSeparators() {
        assertEquals("0712345678", Validation.normalizePhone(" 071 234-5678 "));
        assertEquals("0211234567", Validation.normalizePhone("(021) 123 4567"));
        assertEquals("", Validation.normalizePhone(null));
    }

    @Test
    void isNotEmpty() {
        assertTrue(Validation.isNotEmpty("a"));
        assertFalse(Validation.isNotEmpty("   "));
        assertFalse(Validation.isNotEmpty(""));
        assertFalse(Validation.isNotEmpty(null));
    }
}