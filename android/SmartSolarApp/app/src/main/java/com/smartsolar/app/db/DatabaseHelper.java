/*
 * Smart Solar Microgrid Trading System
 * DatabaseHelper.java
 *
 * Member 2 - Native Android Prosumer Application
 * SQLiteOpenHelper managing local database creation, versioning,
 * and table schemas for user session, booking cache, and grid node cache.
 */
package com.smartsolar.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartsolar.app.utils.Constants;

/**
 * Manages the SQLite database for local offline caching and session persistence.
 * Creates and maintains three tables:
 * 1. user_session: Stores the active logged-in user profile and auth token.
 * 2. booking_cache: Caches energy slot bookings for offline viewing.
 * 3. node_cache: Caches microgrid hubs for offline map display and booking selection.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    // =========================================================================
    // Table: user_session
    // =========================================================================
    public static final String TABLE_USER_SESSION = "user_session";
    public static final String COL_USER_NIC = "nic";
    public static final String COL_USER_FULL_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_TOKEN = "token";
    public static final String COL_USER_STATUS = "status";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_ADDRESS = "address";
    public static final String COL_USER_LOGGED_IN_AT = "logged_in_at";

    private static final String CREATE_TABLE_USER_SESSION =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_SESSION + " ("
                    + COL_USER_NIC + " TEXT PRIMARY KEY, "
                    + COL_USER_FULL_NAME + " TEXT, "
                    + COL_USER_EMAIL + " TEXT, "
                    + COL_USER_ROLE + " TEXT, "
                    + COL_USER_TOKEN + " TEXT, "
                    + COL_USER_STATUS + " TEXT, "
                    + COL_USER_PHONE + " TEXT, "
                    + COL_USER_ADDRESS + " TEXT, "
                    + COL_USER_LOGGED_IN_AT + " INTEGER"
                    + ");";

    // =========================================================================
    // Table: booking_cache
    // =========================================================================
    public static final String TABLE_BOOKING_CACHE = "booking_cache";
    public static final String COL_BOOKING_ID = "booking_id";
    public static final String COL_BOOKING_PROSUMER_NIC = "prosumer_nic";
    public static final String COL_BOOKING_NODE_ID = "node_id";
    public static final String COL_BOOKING_NODE_NAME = "node_name";
    public static final String COL_BOOKING_SLOT_DATE = "slot_date";
    public static final String COL_BOOKING_SLOT_TIME = "slot_time";
    public static final String COL_BOOKING_ENERGY_KWH = "energy_kwh";
    public static final String COL_BOOKING_STATUS = "status";
    public static final String COL_BOOKING_NOTES = "notes";
    public static final String COL_BOOKING_QR_TOKEN = "qr_token";
    public static final String COL_BOOKING_CREATED_AT = "created_at";
    public static final String COL_BOOKING_UPDATED_AT = "updated_at";
    public static final String COL_BOOKING_CACHED_AT = "cached_at";

    private static final String CREATE_TABLE_BOOKING_CACHE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKING_CACHE + " ("
                    + COL_BOOKING_ID + " TEXT PRIMARY KEY, "
                    + COL_BOOKING_PROSUMER_NIC + " TEXT, "
                    + COL_BOOKING_NODE_ID + " TEXT, "
                    + COL_BOOKING_NODE_NAME + " TEXT, "
                    + COL_BOOKING_SLOT_DATE + " TEXT, "
                    + COL_BOOKING_SLOT_TIME + " TEXT, "
                    + COL_BOOKING_ENERGY_KWH + " REAL, "
                    + COL_BOOKING_STATUS + " TEXT, "
                    + COL_BOOKING_NOTES + " TEXT, "
                    + COL_BOOKING_QR_TOKEN + " TEXT, "
                    + COL_BOOKING_CREATED_AT + " TEXT, "
                    + COL_BOOKING_UPDATED_AT + " TEXT, "
                    + COL_BOOKING_CACHED_AT + " INTEGER"
                    + ");";

    // =========================================================================
    // Table: node_cache
    // =========================================================================
    public static final String TABLE_NODE_CACHE = "node_cache";
    public static final String COL_NODE_ID = "node_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_NODE_LOCATION = "location";
    public static final String COL_NODE_LATITUDE = "latitude";
    public static final String COL_NODE_LONGITUDE = "longitude";
    public static final String COL_NODE_CAPACITY_KWH = "capacity_kwh";
    public static final String COL_NODE_BATTERY_SLOTS = "battery_slots";
    public static final String COL_NODE_AVAILABLE_SLOTS = "available_slots";
    public static final String COL_NODE_STATUS = "status";
    public static final String COL_NODE_IS_ACTIVE = "is_active";
    public static final String COL_NODE_SCHEDULE = "schedule";
    public static final String COL_NODE_CREATED_AT = "created_at";
    public static final String COL_NODE_CACHED_AT = "cached_at";

    private static final String CREATE_TABLE_NODE_CACHE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NODE_CACHE + " ("
                    + COL_NODE_ID + " TEXT PRIMARY KEY, "
                    + COL_NODE_NAME + " TEXT, "
                    + COL_NODE_LOCATION + " TEXT, "
                    + COL_NODE_LATITUDE + " REAL, "
                    + COL_NODE_LONGITUDE + " REAL, "
                    + COL_NODE_CAPACITY_KWH + " REAL, "
                    + COL_NODE_BATTERY_SLOTS + " INTEGER, "
                    + COL_NODE_AVAILABLE_SLOTS + " INTEGER, "
                    + COL_NODE_STATUS + " TEXT, "
                    + COL_NODE_IS_ACTIVE + " INTEGER, "
                    + COL_NODE_SCHEDULE + " TEXT, "
                    + COL_NODE_CREATED_AT + " TEXT, "
                    + COL_NODE_CACHED_AT + " INTEGER"
                    + ");";

    /**
     * Public constructor.
     *
     * @param context Application or activity context.
     */
    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    /**
     * Singleton accessor for DatabaseHelper.
     *
     * @param context Application context.
     * @return DatabaseHelper singleton instance.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_SESSION);
        db.execSQL(CREATE_TABLE_BOOKING_CACHE);
        db.execSQL(CREATE_TABLE_NODE_CACHE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables and recreate on schema upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKING_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODE_CACHE);
        onCreate(db);
    }

    /**
     * Clears all cached tables while preserving schemas.
     * Useful on user logout to remove cached user-specific data.
     */
    public void clearAllTables() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_USER_SESSION, null, null);
            db.delete(TABLE_BOOKING_CACHE, null, null);
            db.delete(TABLE_NODE_CACHE, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
