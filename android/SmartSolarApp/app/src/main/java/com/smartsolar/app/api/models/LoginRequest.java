/*
 * Smart Solar Microgrid Trading System
 * LoginRequest.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for sending login credentials to the authentication endpoint.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body sent to POST /api/auth/login.
 * Supports login via NIC or email with password.
 */
public class LoginRequest {

    @SerializedName("nic")
    private String nic;

    @SerializedName("email")
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
