/*
 * Smart Solar Microgrid Trading System
 * NodeCacheDao.java
 *
 * Member 2 - Native Android Prosumer Application
 * Data Access Object for local caching of microgrid nodes/hubs.
 */
package com.smartsolar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.app.api.models.MicrogridNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides caching and querying operations for microgrid nodes in SQLite.
 * Used for offline Google Maps display and booking node selection dropdowns.
 */
public class NodeCacheDao {

    private final DatabaseHelper dbHelper;

    /**
     * Constructor accepting DatabaseHelper instance.
     *
     * @param dbHelper The application database helper.
     */
    public NodeCacheDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Constructor accepting Context.
     *
     * @param context Application context.
     */
    public NodeCacheDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Inserts or updates a single microgrid node in the cache.
     *
     * @param node MicrogridNode object to store.
     * @return Row ID of inserted record, or -1 on error.
     */
    public long insertNode(MicrogridNode node) {
        if (node == null || node.getNodeId() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createContentValues(node);

        return db.insertWithOnConflict(
                DatabaseHelper.TABLE_NODE_CACHE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Inserts a list of microgrid nodes into the cache in a single atomic transaction.
     *
     * @param nodes List of nodes fetched from the API.
     */
    public void insertNodes(List<MicrogridNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (MicrogridNode node : nodes) {
                if (node != null && node.getNodeId() != null) {
                    ContentValues values = createContentValues(node);
                    db.insertWithOnConflict(
                            DatabaseHelper.TABLE_NODE_CACHE,
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
     * Retrieves all cached microgrid nodes.
     *
     * @return List of all MicrogridNode objects.
     */
    public List<MicrogridNode> getAllNodes() {
        List<MicrogridNode> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = DatabaseHelper.COL_NODE_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NODE_CACHE,
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
                    list.add(cursorToNode(cursor));
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
     * Retrieves all active microgrid nodes available for booking.
     *
     * @return List of active MicrogridNode objects.
     */
    public List<MicrogridNode> getActiveNodes() {
        List<MicrogridNode> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_NODE_IS_ACTIVE + " = 1";
        String orderBy = DatabaseHelper.COL_NODE_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NODE_CACHE,
                null,
                selection,
                null,
                null,
                null,
                orderBy
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToNode(cursor));
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
     * Retrieves a single node by its ID.
     *
     * @param nodeId Target node identifier.
     * @return MicrogridNode if found, null otherwise.
     */
    public MicrogridNode getNodeById(String nodeId) {
        if (nodeId == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_NODE_ID + " = ?";
        String[] selectionArgs = {nodeId};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NODE_CACHE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToNode(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Updates an existing node in the cache.
     *
     * @param node Updated node data.
     * @return Number of rows updated.
     */
    public int updateNode(MicrogridNode node) {
        if (node == null || node.getNodeId() == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createContentValues(node);
        String whereClause = DatabaseHelper.COL_NODE_ID + " = ?";
        String[] whereArgs = {node.getNodeId()};

        return db.update(DatabaseHelper.TABLE_NODE_CACHE, values, whereClause, whereArgs);
    }

    /**
     * Deletes a specific node by ID from the cache.
     *
     * @param nodeId Node identifier to delete.
     * @return Number of rows affected.
     */
    public int deleteNode(String nodeId) {
        if (nodeId == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COL_NODE_ID + " = ?";
        String[] whereArgs = {nodeId};

        return db.delete(DatabaseHelper.TABLE_NODE_CACHE, whereClause, whereArgs);
    }

    /**
     * Clears all cached microgrid nodes.
     *
     * @return Number of rows cleared.
     */
    public int clearNodeCache() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_NODE_CACHE, null, null);
    }

    /**
     * Returns the total count of cached nodes.
     *
     * @return Total cached records.
     */
    public int getNodeCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NODE_CACHE;
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
     * Helper to convert MicrogridNode to ContentValues.
     */
    private ContentValues createContentValues(MicrogridNode node) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NODE_ID, node.getNodeId());
        values.put(DatabaseHelper.COL_NODE_NAME, node.getNodeName());
        values.put(DatabaseHelper.COL_NODE_LOCATION, node.getLocation());
        values.put(DatabaseHelper.COL_NODE_LATITUDE, node.getLatitude());
        values.put(DatabaseHelper.COL_NODE_LONGITUDE, node.getLongitude());
        values.put(DatabaseHelper.COL_NODE_CAPACITY_KWH, node.getCapacityKwh());
        values.put(DatabaseHelper.COL_NODE_BATTERY_SLOTS, node.getBatterySlots());
        values.put(DatabaseHelper.COL_NODE_AVAILABLE_SLOTS, node.getAvailableSlots());
        values.put(DatabaseHelper.COL_NODE_STATUS, node.getStatus());
        values.put(DatabaseHelper.COL_NODE_IS_ACTIVE, node.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_NODE_SCHEDULE, node.getSchedule());
        values.put(DatabaseHelper.COL_NODE_CREATED_AT, node.getCreatedAt());
        values.put(DatabaseHelper.COL_NODE_CACHED_AT, System.currentTimeMillis());
        return values;
    }

    /**
     * Helper to map Cursor row to MicrogridNode instance.
     */
    private MicrogridNode cursorToNode(Cursor cursor) {
        MicrogridNode n = new MicrogridNode();
        n.setNodeId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_ID)));
        n.setNodeName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_NAME)));
        n.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_LOCATION)));
        n.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_LATITUDE)));
        n.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_LONGITUDE)));
        n.setCapacityKwh(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_CAPACITY_KWH)));
        n.setBatterySlots(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_BATTERY_SLOTS)));
        n.setAvailableSlots(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_AVAILABLE_SLOTS)));
        n.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_STATUS)));
        n.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_IS_ACTIVE)) == 1);
        n.setSchedule(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_SCHEDULE)));
        n.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NODE_CREATED_AT)));
        return n;
    }
}
