/*
 * Smart Solar Microgrid Trading System
 * ValidationUtils.java
 *
 * Member 2 - Native Android Prosumer Application
 * Utility class for input validation including NIC format,
 * email, phone number, and required field checks.
 */
package com.smartsolar.app.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Input validation helpers used across registration, profile editing,
 * and booking forms. NIC is the primary key for prosumers.
 */
public final class ValidationUtils {

    // Prevent instantiation
    private ValidationUtils() {
    }

    /**
     * Sri Lankan NIC patterns:
     * Old format: 9 digits followed by V or X (e.g., 912345678V)
     * New format: 12 digits (e.g., 200012345678)
     */
    private static final Pattern NIC_OLD_FORMAT = Pattern.compile("^[0-9]{9}[VvXx]$");
    private static final Pattern NIC_NEW_FORMAT = Pattern.compile("^[0-9]{12}$");

    /** Phone number: at least 10 digits, optional + prefix. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    /** Minimum password length. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Validates a Sri Lankan National Identity Card number.
     * Accepts both old format (9 digits + V/X) and new format (12 digits).
     *
     * @param nic The NIC string to validate.
     * @return true if the NIC matches an accepted format, false otherwise.
     */
    public static boolean isValidNic(String nic) {
        if (TextUtils.isEmpty(nic)) {
            return false;
        }
        String trimmed = nic.trim();
        return NIC_OLD_FORMAT.matcher(trimmed).matches()
                || NIC_NEW_FORMAT.matcher(trimmed).matches();
    }

    /**
     * Validates an email address format.
     *
     * @param email The email string to validate.
     * @return true if the email matches a standard email pattern, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Validates a phone number format.
     * Accepts 10-15 digits with an optional leading '+'.
     *
     * @param phone The phone number string to validate.
     * @return true if the phone number is valid, false otherwise.
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates that a password meets minimum length requirements.
     *
     * @param password The password string to validate.
     * @return true if the password is at least MIN_PASSWORD_LENGTH characters, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Checks that two password strings match (for registration confirmation).
     *
     * @param password        The password entered.
     * @param confirmPassword The confirmation password entered.
     * @return true if both passwords are non-empty and identical, false otherwise.
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Checks that a required field is not empty or blank.
     *
     * @param value The string value to check.
     * @return true if the value is non-null and contains non-whitespace characters.
     */
    public static boolean isNotEmpty(String value) {
        return !TextUtils.isEmpty(value) && !value.trim().isEmpty();
    }

    /**
     * Validates a full name (at least 2 characters, letters and spaces only).
     *
     * @param name The name string to validate.
     * @return true if the name is valid, false otherwise.
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.matches("^[a-zA-Z\\s.'-]+$");
    }

    /**
     * Returns a trimmed version of the string, or empty string if null.
     *
     * @param value The input string.
     * @return Trimmed string or empty string.
     */
    public static String safeTrim(String value) {
        return value != null ? value.trim() : "";
    }
}
