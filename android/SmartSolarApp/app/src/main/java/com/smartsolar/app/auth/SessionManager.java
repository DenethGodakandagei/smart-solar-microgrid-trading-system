/*
 * Smart Solar Microgrid Trading System
 * SessionManager.java
 *
 * Member 2 - Native Android Prosumer Application
 * SharedPreferences-based session manager for persisting authentication token,
 * user identity, role, and login state across app restarts.
 */
package com.smartsolar.app.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.models.LoginResponse;
import com.smartsolar.app.utils.Constants;

/**
 * Manages the user session state using Android SharedPreferences.
 * Stores JWT authentication token, role, NIC, and full name.
 * Provides helper methods for role-based navigation and authentication checks.
 */
public class SessionManager {

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Context context;

    /**
     * Constructor.
     *
     * @param context Application or activity context.
     */
    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        this.editor = this.prefs.edit();
    }

    /**
     * Singleton accessor for SessionManager.
     *
     * @param context Context instance.
     * @return Singleton SessionManager instance.
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Saves complete login session from explicit parameters.
     *
     * @param token    JWT authentication token.
     * @param nic      Prosumer or Operator NIC.
     * @param fullName User display name.
     * @param email    User email address.
     * @param role     User role (Prosumer, Operator, Backoffice).
     */
    public void saveLoginSession(String token, String nic, String fullName, String email, String role) {
        editor.putString(Constants.PREF_AUTH_TOKEN, token);
        editor.putString(Constants.PREF_USER_NIC, nic);
        editor.putString(Constants.PREF_USER_NAME, fullName);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_ROLE, role);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.apply();

        // Refresh API client so next requests immediately attach the new JWT token
        ApiClient.refreshApiService(context);
    }

    /**
     * Saves complete login session from a LoginResponse DTO.
     *
     * @param response LoginResponse returned from POST /api/auth/login.
     */
    public void saveLoginSession(LoginResponse response) {
        if (response == null) return;
        saveLoginSession(
                response.getToken(),
                response.getNic(),
                response.getFullName(),
                response.getEmail(),
                response.getRole()
        );
    }

    /**
     * Updates only the authentication JWT token.
     *
     * @param token New JWT token.
     */
    public void saveAuthToken(String token) {
        editor.putString(Constants.PREF_AUTH_TOKEN, token);
        editor.apply();
        ApiClient.refreshApiService(context);
    }

    /**
     * Returns the stored JWT authentication token.
     *
     * @return Token string or empty string if not logged in.
     */
    public String getAuthToken() {
        return prefs.getString(Constants.PREF_AUTH_TOKEN, "");
    }

    /**
     * Returns the logged-in user's National Identity Card (NIC) number.
     *
     * @return NIC string or empty string.
     */
    public String getUserNic() {
        return prefs.getString(Constants.PREF_USER_NIC, "");
    }

    /**
     * Returns the logged-in user's full name.
     *
     * @return Full name string or empty string.
     */
    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    /**
     * Returns the logged-in user's email address.
     *
     * @return Email string or empty string.
     */
    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, "");
    }

    /**
     * Returns the logged-in user's role (Prosumer, Operator, Backoffice).
     *
     * @return Role string or empty string.
     */
    public String getUserRole() {
        return prefs.getString(Constants.PREF_USER_ROLE, "");
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if user is logged in and token is non-empty.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
                && !getAuthToken().isEmpty();
    }

    /**
     * Checks if the logged-in user has the Prosumer role.
     *
     * @return true if user role is Prosumer.
     */
    public boolean isProsumer() {
        return Constants.ROLE_PROSUMER.equalsIgnoreCase(getUserRole());
    }

    /**
     * Checks if the logged-in user has the Operator role.
     *
     * @return true if user role is Operator.
     */
    public boolean isOperator() {
        return Constants.ROLE_OPERATOR.equalsIgnoreCase(getUserRole());
    }

    /**
     * Checks if the logged-in user has the Backoffice role.
     *
     * @return true if user role is Backoffice.
     */
    public boolean isBackoffice() {
        return Constants.ROLE_BACKOFFICE.equalsIgnoreCase(getUserRole());
    }

    /**
     * Updates user profile fields in SharedPreferences after profile edit.
     *
     * @param fullName Updated full name.
     * @param email    Updated email.
     */
    public void updateUserProfile(String fullName, String email) {
        editor.putString(Constants.PREF_USER_NAME, fullName);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Clears all session preferences and resets the ApiClient.
     */
    public void logout() {
        editor.clear();
        editor.apply();
        ApiClient.reset();
    }
}
