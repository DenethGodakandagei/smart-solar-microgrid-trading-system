/*
 * Smart Solar Microgrid Trading System
 * Constants.java
 *
 * Member 2 - Native Android Prosumer Application
 * Centralized application constants including API base URL,
 * SharedPreferences keys, date formats, and request codes.
 */
package com.smartsolar.app.utils;

/**
 * Application-wide constants used across all modules.
 * Modify BASE_URL to point to your deployed C# Web API on IIS.
 */
public final class Constants {

    // Prevent instantiation
    private Constants() {
    }

    // ---------------------------------------------------------------
    // API Configuration
    // ---------------------------------------------------------------

    /**
     * Base URL for the C# Web API hosted on IIS.
     * For Android Emulator use 10.0.2.2 to reach localhost.
     * For physical device use the actual server IP address.
     */
    public static final String BASE_URL = "http://10.0.2.2:5000/api/";

    /** Connection timeout in seconds for API requests. */
    public static final int CONNECT_TIMEOUT = 30;

    /** Read timeout in seconds for API responses. */
    public static final int READ_TIMEOUT = 30;

    /** Write timeout in seconds for API request bodies. */
    public static final int WRITE_TIMEOUT = 30;

    // ---------------------------------------------------------------
    // SharedPreferences
    // ---------------------------------------------------------------

    /** SharedPreferences file name for session data. */
    public static final String PREF_NAME = "SmartSolarPrefs";

    /** Key for storing the authentication token. */
    public static final String PREF_AUTH_TOKEN = "auth_token";

    /** Key for storing the logged-in user's NIC. */
    public static final String PREF_USER_NIC = "user_nic";

    /** Key for storing the logged-in user's full name. */
    public static final String PREF_USER_NAME = "user_name";

    /** Key for storing the logged-in user's email. */
    public static final String PREF_USER_EMAIL = "user_email";

    /** Key for storing the logged-in user's role (Prosumer/Operator). */
    public static final String PREF_USER_ROLE = "user_role";

    /** Key for storing whether the user is currently logged in. */
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";

    // ---------------------------------------------------------------
    // User Roles
    // ---------------------------------------------------------------

    /** Role identifier for solar prosumer users. */
    public static final String ROLE_PROSUMER = "Prosumer";

    /** Role identifier for grid operator users. */
    public static final String ROLE_OPERATOR = "Operator";

    /** Role identifier for backoffice admin users. */
    public static final String ROLE_BACKOFFICE = "Backoffice";

    // ---------------------------------------------------------------
    // Booking Status
    // ---------------------------------------------------------------

    /** Booking status when newly created and awaiting approval. */
    public static final String STATUS_PENDING = "Pending";

    /** Booking status when approved by the system or operator. */
    public static final String STATUS_APPROVED = "Approved";

    /** Booking status when cancelled by user or operator. */
    public static final String STATUS_CANCELLED = "Cancelled";

    /** Booking status when energy transfer is completed. */
    public static final String STATUS_COMPLETED = "Completed";

    // ---------------------------------------------------------------
    // Business Rules
    // ---------------------------------------------------------------

    /** Maximum number of days in advance a booking can be scheduled. */
    public static final int BOOKING_MAX_DAYS_AHEAD = 7;

    /** Minimum hours' notice required for booking updates or cancellations. */
    public static final int BOOKING_MIN_HOURS_NOTICE = 12;

    // ---------------------------------------------------------------
    // Date & Time Formats
    // ---------------------------------------------------------------

    /** Standard date format used for API communication. */
    public static final String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /** Display date format for UI. */
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";

    /** Display time format for UI. */
    public static final String TIME_FORMAT_DISPLAY = "hh:mm a";

    /** Display date-time format for UI. */
    public static final String DATETIME_FORMAT_DISPLAY = "dd MMM yyyy, hh:mm a";

    // ---------------------------------------------------------------
    // Intent Extras
    // ---------------------------------------------------------------

    /** Intent extra key for passing booking ID between activities. */
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    /** Intent extra key for passing node ID between activities. */
    public static final String EXTRA_NODE_ID = "extra_node_id";

    /** Intent extra key for passing QR data between activities. */
    public static final String EXTRA_QR_DATA = "extra_qr_data";

    /** Intent extra key for passing action type (create/update/cancel). */
    public static final String EXTRA_ACTION_TYPE = "extra_action_type";

    // ---------------------------------------------------------------
    // Request Codes
    // ---------------------------------------------------------------

    /** Request code for QR code scanning activity. */
    public static final int REQUEST_QR_SCAN = 1001;

    /** Request code for camera permission. */
    public static final int REQUEST_CAMERA_PERMISSION = 1002;

    /** Request code for location permission. */
    public static final int REQUEST_LOCATION_PERMISSION = 1003;

    // ---------------------------------------------------------------
    // SQLite Database
    // ---------------------------------------------------------------

    /** Local SQLite database name. */
    public static final String DB_NAME = "smart_solar.db";

    /** Local SQLite database version. */
    public static final int DB_VERSION = 1;
}
