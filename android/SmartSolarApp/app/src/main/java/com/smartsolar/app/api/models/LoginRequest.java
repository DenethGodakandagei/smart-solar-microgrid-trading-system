package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body sent to POST /api/auth/login.
 * Supports login via NIC with password.
 */
public class LoginRequest {

    @SerializedName("nic")
    private String nic;

    @SerializedName("password")
    private String password;

    /** Default constructor for Gson. */
    public LoginRequest() {
    }

    /**
     * Creates a login request with NIC and password.
     *
     * @param nic      National Identity Card number.
     * @param password User's password.
     */
    public LoginRequest(String nic, String password) {
        this.nic = nic;
        this.password = password;
    }

    // Getters and Setters

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}