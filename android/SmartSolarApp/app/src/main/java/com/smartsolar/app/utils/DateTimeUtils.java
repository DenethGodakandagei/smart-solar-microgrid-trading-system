/*
 * Smart Solar Microgrid Trading System
 * DateTimeUtils.java
 *
 * Member 2 - Native Android Prosumer Application
 * Utility class for date/time operations including business rule
 * validation for the 7-day booking window and 12-hour cancellation notice.
 */
package com.smartsolar.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Date and time helper methods for booking validations and display formatting.
 * Business rules: bookings within 7 days, updates/cancellations need 12-hour notice.
 */
public final class DateTimeUtils {

    // Prevent instantiation
    private DateTimeUtils() {
    }

    /**
     * Checks if a given date falls within the allowed 7-day booking window.
     *
     * @param bookingDate The proposed booking date.
     * @return true if the date is today or within the next 7 days, false otherwise.
     */
    public static boolean isWithin7DayWindow(Date bookingDate) {
        if (bookingDate == null) {
            return false;
        }

        Date now = new Date();

        // Booking date must not be in the past
        if (bookingDate.before(now)) {
            return false;
        }

        // Calculate max allowed date (7 days from now)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, Constants.BOOKING_MAX_DAYS_AHEAD);
        Date maxDate = calendar.getTime();

        return !bookingDate.after(maxDate);
    }

    /**
     * Checks if a booking slot is at least 12 hours away from now.
     * Required for updates and cancellations.
     *
     * @param slotDateTime The date/time of the booking slot.
     * @return true if at least 12 hours remain before the slot, false otherwise.
     */
    public static boolean hasMinimum12HourNotice(Date slotDateTime) {
        if (slotDateTime == null) {
            return false;
        }

        Date now = new Date();
        long diffMillis = slotDateTime.getTime() - now.getTime();
        long diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis);

        return diffHours >= Constants.BOOKING_MIN_HOURS_NOTICE;
    }

    /**
     * Formats a Date object to the API-expected ISO 8601 format.
     *
     * @param date The date to format.
     * @return Formatted date string for API communication.
     */
    public static String formatForApi(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * Parses an API date string back to a Date object.
     *
     * @param dateString The ISO 8601 date string from the API.
     * @return Parsed Date object, or null if parsing fails.
     */
    public static Date parseApiDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Formats a Date for display in the UI (e.g., "20 Sep 2026").
     *
     * @param date The date to format.
     * @return Human-readable date string.
     */
    public static String formatDisplayDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
        return sdf.format(date);
    }

    /**
     * Formats a Date for display as time only (e.g., "02:30 PM").
     *
     * @param date The date to format.
     * @return Human-readable time string.
     */
    public static String formatDisplayTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.US);
        return sdf.format(date);
    }

    /**
     * Formats a Date for full date-time display (e.g., "20 Sep 2026, 02:30 PM").
     *
     * @param date The date to format.
     * @return Human-readable date-time string.
     */
    public static String formatDisplayDateTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT_DISPLAY, Locale.US);
        return sdf.format(date);
    }

    /**
     * Formats a date string from API format to display format.
     *
     * @param apiDateString The date string in API format.
     * @return Human-readable date string, or the original string if parsing fails.
     */
    public static String formatApiDateForDisplay(String apiDateString) {
        Date date = parseApiDate(apiDateString);
        if (date != null) {
            return formatDisplayDateTime(date);
        }
        return apiDateString != null ? apiDateString : "N/A";
    }

    /**
     * Calculates the number of hours remaining until a given date.
     *
     * @param futureDate A date in the future.
     * @return Number of hours remaining, or 0 if the date is in the past.
     */
    public static long hoursUntil(Date futureDate) {
        if (futureDate == null) {
            return 0;
        }
        long diffMillis = futureDate.getTime() - new Date().getTime();
        if (diffMillis <= 0) {
            return 0;
        }
        return TimeUnit.MILLISECONDS.toHours(diffMillis);
    }

    /**
     * Gets the current date with time set to midnight (start of day).
     *
     * @return Today's date at 00:00:00.
     */
    public static Date getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Gets the maximum allowed booking date (7 days from today at end of day).
     *
     * @return The latest date a booking can be scheduled for.
     */
    public static Date getMaxBookingDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, Constants.BOOKING_MAX_DAYS_AHEAD);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }
}
