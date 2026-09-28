/*
 * Smart Solar Microgrid Trading System
 * UserDao.java
 *
 * Member 2 - Native Android Prosumer Application
 * Data Access Object for local user session persistence in SQLite.
 */
package com.smartsolar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.app.api.models.LoginResponse;
import com.smartsolar.app.api.models.ProsumerProfile;

/**
 * Provides CRUD operations for the local `user_session` SQLite table.
 * Used to persist user credentials and profile state across app lifecycles.
 */
public class UserDao {

    private final DatabaseHelper dbHelper;

    /**
     * Constructor accepting DatabaseHelper instance.
     *
     * @param dbHelper The application database helper.
     */
    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Constructor accepting Context.
     *
     * @param context Application context.
     */
    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Inserts or replaces a user session from a LoginResponse.
     *
     * @param loginResponse The response received from the authentication API.
     * @return Row ID of the inserted record, or -1 if an error occurred.
     */
    public long saveSession(LoginResponse loginResponse) {
        if (loginResponse == null || loginResponse.getNic() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_NIC, loginResponse.getNic());
        values.put(DatabaseHelper.COL_USER_FULL_NAME, loginResponse.getFullName());
        values.put(DatabaseHelper.COL_USER_EMAIL, loginResponse.getEmail());
        values.put(DatabaseHelper.COL_USER_ROLE, loginResponse.getRole());
        values.put(DatabaseHelper.COL_USER_TOKEN, loginResponse.getToken());
        values.put(DatabaseHelper.COL_USER_STATUS, loginResponse.getStatus());
        values.put(DatabaseHelper.COL_USER_LOGGED_IN_AT, System.currentTimeMillis());

        return db.insertWithOnConflict(
                DatabaseHelper.TABLE_USER_SESSION,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Inserts or replaces a user session from a ProsumerProfile.
     *
     * @param profile Prosumer profile information.
     * @param token   Authentication JWT token.
     * @return Row ID of the inserted record, or -1 if an error occurred.
     */
    public long saveSession(ProsumerProfile profile, String token) {
        if (profile == null || profile.getNic() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_NIC, profile.getNic());
        values.put(DatabaseHelper.COL_USER_FULL_NAME, profile.getFullName());
        values.put(DatabaseHelper.COL_USER_EMAIL, profile.getEmail());
        values.put(DatabaseHelper.COL_USER_ROLE, profile.getRole());
        values.put(DatabaseHelper.COL_USER_TOKEN, token);
        values.put(DatabaseHelper.COL_USER_STATUS, profile.getStatus());
        values.put(DatabaseHelper.COL_USER_PHONE, profile.getPhone());
        values.put(DatabaseHelper.COL_USER_ADDRESS, profile.getAddress());
        values.put(DatabaseHelper.COL_USER_LOGGED_IN_AT, System.currentTimeMillis());

        return db.insertWithOnConflict(
                DatabaseHelper.TABLE_USER_SESSION,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Inserts or replaces raw user session data.
     *
     * @return Row ID of the inserted record.
     */
    public long insertOrUpdateUser(String nic, String fullName, String email, String role,
                                   String token, String status, String phone, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_NIC, nic);
        values.put(DatabaseHelper.COL_USER_FULL_NAME, fullName);
        values.put(DatabaseHelper.COL_USER_EMAIL, email);
        values.put(DatabaseHelper.COL_USER_ROLE, role);
        values.put(DatabaseHelper.COL_USER_TOKEN, token);
        values.put(DatabaseHelper.COL_USER_STATUS, status);
        values.put(DatabaseHelper.COL_USER_PHONE, phone);
        values.put(DatabaseHelper.COL_USER_ADDRESS, address);
        values.put(DatabaseHelper.COL_USER_LOGGED_IN_AT, System.currentTimeMillis());

        return db.insertWithOnConflict(
                DatabaseHelper.TABLE_USER_SESSION,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Retrieves the current logged in user as a LoginResponse DTO.
     *
     * @return LoginResponse if a session exists, null otherwise.
     */
    public LoginResponse getLoggedInUser() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER_SESSION
                + " ORDER BY " + DatabaseHelper.COL_USER_LOGGED_IN_AT + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                LoginResponse user = new LoginResponse();
                user.setNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NIC)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULL_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE)));
                user.setToken(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_TOKEN)));
                user.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_STATUS)));
                return user;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Retrieves the prosumer profile by NIC from the local session table.
     *
     * @param nic National Identity Card number.
     * @return ProsumerProfile if found, null otherwise.
     */
    public ProsumerProfile getProsumerProfile(String nic) {
        if (nic == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_USER_NIC + " = ?";
        String[] selectionArgs = {nic};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_SESSION,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                ProsumerProfile profile = new ProsumerProfile();
                profile.setNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NIC)));
                profile.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULL_NAME)));
                profile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
                profile.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE)));
                profile.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_STATUS)));
                profile.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE)));
                profile.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ADDRESS)));
                return profile;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Retrieves the NIC of the currently active logged-in user.
     *
     * @return NIC string if logged in, null otherwise.
     */
    public String getActiveUserNic() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COL_USER_NIC};
        String orderBy = DatabaseHelper.COL_USER_LOGGED_IN_AT + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_SESSION,
                columns,
                null,
                null,
                null,
                null,
                orderBy,
                "1"
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NIC));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Retrieves the auth JWT token for the current session.
     *
     * @return Token string if found, null otherwise.
     */
    public String getActiveAuthToken() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COL_USER_TOKEN};
        String orderBy = DatabaseHelper.COL_USER_LOGGED_IN_AT + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_SESSION,
                columns,
                null,
                null,
                null,
                null,
                orderBy,
                "1"
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_TOKEN));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Updates profile details in the local session.
     *
     * @param nic      Target user's NIC.
     * @param fullName New full name.
     * @param phone    New phone number.
     * @param address  New address.
     * @return Number of rows affected.
     */
    public int updateUserProfile(String nic, String fullName, String phone, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_FULL_NAME, fullName);
        values.put(DatabaseHelper.COL_USER_PHONE, phone);
        values.put(DatabaseHelper.COL_USER_ADDRESS, address);

        String whereClause = DatabaseHelper.COL_USER_NIC + " = ?";
        String[] whereArgs = {nic};

        return db.update(DatabaseHelper.TABLE_USER_SESSION, values, whereClause, whereArgs);
    }

    /**
     * Checks if a local user session exists.
     *
     * @return true if at least one session record exists, false otherwise.
     */
    public boolean hasActiveSession() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USER_SESSION;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Deletes a user session by NIC.
     *
     * @param nic The NIC of the user session to remove.
     * @return Number of rows deleted.
     */
    public int deleteSession(String nic) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COL_USER_NIC + " = ?";
        String[] whereArgs = {nic};
        return db.delete(DatabaseHelper.TABLE_USER_SESSION, whereClause, whereArgs);
    }

    /**
     * Deletes all sessions from the table (used on logout).
     *
     * @return Number of rows deleted.
     */
    public int clearAllSessions() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_USER_SESSION, null, null);
    }
}
