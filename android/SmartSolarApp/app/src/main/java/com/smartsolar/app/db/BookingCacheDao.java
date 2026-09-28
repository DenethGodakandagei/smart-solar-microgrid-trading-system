/*
 * Smart Solar Microgrid Trading System
 * BookingCacheDao.java
 *
 * Member 2 - Native Android Prosumer Application
 * Data Access Object for local caching of energy slot reservations.
 */
package com.smartsolar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.app.api.models.BookingResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides caching and querying operations for bookings in SQLite.
 * Enables offline viewing of pending, approved, and completed bookings.
 */
public class BookingCacheDao {

    private final DatabaseHelper dbHelper;

    /**
     * Constructor accepting DatabaseHelper instance.
     *
     * @param dbHelper The application database helper.
     */
    public BookingCacheDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Constructor accepting Context.
     *
     * @param context Application context.
     */
    public BookingCacheDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Inserts or updates a single booking in the cache.
     *
     * @param booking BookingResponse object to store.
     * @return Row ID of inserted record, or -1 on error.
     */
    public long insertBooking(BookingResponse booking) {
        if (booking == null || booking.getBookingId() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createContentValues(booking);

        return db.insertWithOnConflict(
                DatabaseHelper.TABLE_BOOKING_CACHE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Inserts a list of bookings into the cache in a single atomic transaction.
     *
     * @param bookings List of bookings from the API.
     */
    public void insertBookings(List<BookingResponse> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (BookingResponse booking : bookings) {
                if (booking != null && booking.getBookingId() != null) {
                    ContentValues values = createContentValues(booking);
                    db.insertWithOnConflict(
                            DatabaseHelper.TABLE_BOOKING_CACHE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Retrieves all cached bookings sorted by slot date and time descending.
     *
     * @return List of BookingResponse items.
     */
    public List<BookingResponse> getAllBookings() {
        List<BookingResponse> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = DatabaseHelper.COL_BOOKING_SLOT_DATE + " DESC, "
                + DatabaseHelper.COL_BOOKING_SLOT_TIME + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKING_CACHE,
                null,
                null,
                null,
                null,
                null,
                orderBy
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToBooking(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Retrieves cached bookings belonging to a specific prosumer NIC.
     *
     * @param nic Prosumer NIC number.
     * @return Filtered list of BookingResponse items.
     */
    public List<BookingResponse> getBookingsByNic(String nic) {
        List<BookingResponse> list = new ArrayList<>();
        if (nic == null) return list;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_BOOKING_PROSUMER_NIC + " = ?";
        String[] selectionArgs = {nic};
        String orderBy = DatabaseHelper.COL_BOOKING_SLOT_DATE + " DESC, "
                + DatabaseHelper.COL_BOOKING_SLOT_TIME + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKING_CACHE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToBooking(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Retrieves cached bookings with a specific status (e.g., "Pending", "Approved").
     *
     * @param status Status filter.
     * @return Filtered list of BookingResponse items.
     */
    public List<BookingResponse> getBookingsByStatus(String status) {
        List<BookingResponse> list = new ArrayList<>();
        if (status == null) return list;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_BOOKING_STATUS + " = ?";
        String[] selectionArgs = {status};
        String orderBy = DatabaseHelper.COL_BOOKING_SLOT_DATE + " DESC, "
                + DatabaseHelper.COL_BOOKING_SLOT_TIME + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKING_CACHE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToBooking(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Retrieves a single booking by ID.
     *
     * @param bookingId Target booking identifier.
     * @return BookingResponse if found, null otherwise.
     */
    public BookingResponse getBookingById(String bookingId) {
        if (bookingId == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_BOOKING_ID + " = ?";
        String[] selectionArgs = {bookingId};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKING_CACHE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToBooking(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Updates an existing booking in the cache.
     *
     * @param booking Updated booking data.
     * @return Number of rows updated.
     */
    public int updateBooking(BookingResponse booking) {
        if (booking == null || booking.getBookingId() == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createContentValues(booking);
        String whereClause = DatabaseHelper.COL_BOOKING_ID + " = ?";
        String[] whereArgs = {booking.getBookingId()};

        return db.update(DatabaseHelper.TABLE_BOOKING_CACHE, values, whereClause, whereArgs);
    }

    /**
     * Deletes a specific booking by ID from the cache.
     *
     * @param bookingId Booking identifier to delete.
     * @return Number of rows affected.
     */
    public int deleteBooking(String bookingId) {
        if (bookingId == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COL_BOOKING_ID + " = ?";
        String[] whereArgs = {bookingId};

        return db.delete(DatabaseHelper.TABLE_BOOKING_CACHE, whereClause, whereArgs);
    }

    /**
     * Clears all cached bookings.
     *
     * @return Number of rows cleared.
     */
    public int clearBookingCache() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_BOOKING_CACHE, null, null);
    }

    /**
     * Clears cached bookings for a specific user NIC.
     *
     * @param nic User NIC.
     * @return Number of rows cleared.
     */
    public int clearBookingsForUser(String nic) {
        if (nic == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COL_BOOKING_PROSUMER_NIC + " = ?";
        String[] whereArgs = {nic};

        return db.delete(DatabaseHelper.TABLE_BOOKING_CACHE, whereClause, whereArgs);
    }

    /**
     * Returns the total count of cached bookings.
     *
     * @return Total cached records.
     */
    public int getBookingCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BOOKING_CACHE;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Helper to convert BookingResponse to ContentValues.
     */
    private ContentValues createContentValues(BookingResponse booking) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BOOKING_ID, booking.getBookingId());
        values.put(DatabaseHelper.COL_BOOKING_PROSUMER_NIC, booking.getProsumerNic());
        values.put(DatabaseHelper.COL_BOOKING_NODE_ID, booking.getNodeId());
        values.put(DatabaseHelper.COL_BOOKING_NODE_NAME, booking.getNodeName());
        values.put(DatabaseHelper.COL_BOOKING_SLOT_DATE, booking.getSlotDate());
        values.put(DatabaseHelper.COL_BOOKING_SLOT_TIME, booking.getSlotTime());
        values.put(DatabaseHelper.COL_BOOKING_ENERGY_KWH, booking.getEnergyKwh());
        values.put(DatabaseHelper.COL_BOOKING_STATUS, booking.getStatus());
        values.put(DatabaseHelper.COL_BOOKING_NOTES, booking.getNotes());
        values.put(DatabaseHelper.COL_BOOKING_QR_TOKEN, booking.getQrToken());
        values.put(DatabaseHelper.COL_BOOKING_CREATED_AT, booking.getCreatedAt());
        values.put(DatabaseHelper.COL_BOOKING_UPDATED_AT, booking.getUpdatedAt());
        values.put(DatabaseHelper.COL_BOOKING_CACHED_AT, System.currentTimeMillis());
        return values;
    }

    /**
     * Helper to map Cursor row to BookingResponse instance.
     */
    private BookingResponse cursorToBooking(Cursor cursor) {
        BookingResponse b = new BookingResponse();
        b.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_ID)));
        b.setProsumerNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_PROSUMER_NIC)));
        b.setNodeId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_NODE_ID)));
        b.setNodeName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_NODE_NAME)));
        b.setSlotDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_SLOT_DATE)));
        b.setSlotTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_SLOT_TIME)));
        b.setEnergyKwh(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_ENERGY_KWH)));
        b.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_STATUS)));
        b.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_NOTES)));
        b.setQrToken(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_QR_TOKEN)));
        b.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_CREATED_AT)));
        b.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_UPDATED_AT)));
        return b;
    }
}
