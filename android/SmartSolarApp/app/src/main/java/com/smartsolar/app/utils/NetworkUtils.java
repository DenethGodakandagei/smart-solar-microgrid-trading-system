/*
 * Smart Solar Microgrid Trading System
 * NetworkUtils.java
 *
 * Member 2 - Native Android Prosumer Application
 * Utility class to check network connectivity status before making API calls.
 */
package com.smartsolar.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Provides network connectivity checks.
 * Used before API calls to show appropriate offline messages.
 */
public final class NetworkUtils {

    // Prevent instantiation
    private NetworkUtils() {
    }

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @param context Application or activity context.
     * @return true if the device has internet connectivity, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);

        if (capabilities == null) {
            return false;
        }

        // Check for any of the supported transport types
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    /**
     * Checks if Wi-Fi is the current active transport.
     *
     * @param context Application or activity context.
     * @return true if connected via Wi-Fi, false otherwise.
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);

        if (capabilities == null) {
            return false;
        }

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}
