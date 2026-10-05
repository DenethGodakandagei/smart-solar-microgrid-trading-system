/*
 * Smart Solar Microgrid Trading System
 * SmartSolarApplication.java
 *
 * Member 2 - Native Android Prosumer Application
 * Application class for initializing singletons, SQLite database helper,
 * DAOs, and SessionManager on app launch.
 */
package com.smartsolar.app;

import android.app.Application;
import android.content.Context;

import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.db.DatabaseHelper;
import com.smartsolar.app.db.NodeCacheDao;
import com.smartsolar.app.db.UserDao;

/**
 * Base Application class for Smart Solar Microgrid Trading System.
 * Initializes core singletons on process startup:
 * - SQLite Database & DAOs (User, BookingCache, NodeCache)
 * - SharedPreferences SessionManager
 * - Retrofit API Client
 */
public class SmartSolarApplication extends Application {

    private static SmartSolarApplication instance;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private UserDao userDao;
    private BookingCacheDao bookingCacheDao;
    private NodeCacheDao nodeCacheDao;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize SQLite database helper & singletons
        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Initialize DAOs
        userDao = new UserDao(databaseHelper);
        bookingCacheDao = new BookingCacheDao(databaseHelper);
        nodeCacheDao = new NodeCacheDao(databaseHelper);
    }

    /**
     * Returns the singleton instance of the application.
     *
     * @return SmartSolarApplication instance.
     */
    public static SmartSolarApplication getInstance() {
        return instance;
    }

    /**
     * Convenient static getter for application context.
     *
     * @return Application context.
     */
    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }

    /**
     * Returns the SQLite DatabaseHelper singleton.
     *
     * @return DatabaseHelper instance.
     */
    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    /**
     * Returns the SessionManager singleton.
     *
     * @return SessionManager instance.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Returns the UserDao instance.
     *
     * @return UserDao instance.
     */
    public UserDao getUserDao() {
        return userDao;
    }

    /**
     * Returns the BookingCacheDao instance.
     *
     * @return BookingCacheDao instance.
     */
    public BookingCacheDao getBookingCacheDao() {
        return bookingCacheDao;
    }

    /**
     * Returns the NodeCacheDao instance.
     *
     * @return NodeCacheDao instance.
     */
    public NodeCacheDao getNodeCacheDao() {
        return nodeCacheDao;
    }

    /**
     * Performs a full logout: clears SharedPreferences, resets API client,
     * and clears cached local SQLite data.
     */
    public void performLogout() {
        if (sessionManager != null) {
            sessionManager.logout();
        }
        if (userDao != null) {
            userDao.clearAllSessions();
        }
        if (bookingCacheDao != null) {
            bookingCacheDao.clearBookingCache();
        }
        if (nodeCacheDao != null) {
            nodeCacheDao.clearNodeCache();
        }
    }
}
