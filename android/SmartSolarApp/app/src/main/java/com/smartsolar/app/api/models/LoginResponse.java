/*
 * Smart Solar Microgrid Trading System
 * LoginResponse.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for receiving authentication response containing JWT token,
 * user role, and basic profile information.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Response from POST /api/auth/login.
 * Contains JWT token for subsequent authenticated requests,
 * user role for routing, and basic profile data for local caching.
 */
public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("nic")
    private String nic;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    /** Default constructor for Gson. */
    public LoginResponse() {
    }

    // Getters and Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
