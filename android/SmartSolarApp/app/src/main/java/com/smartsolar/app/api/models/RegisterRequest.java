/*
 * Smart Solar Microgrid Trading System
 * RegisterRequest.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for sending prosumer registration data to the API.
 * NIC serves as the primary key for prosumer accounts.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body sent to POST /api/auth/register.
 * All fields except address are required.
 * NIC is validated as the primary key for prosumer identity.
 */
public class RegisterRequest {

    @SerializedName("nic")
    private String nic;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("password")
    private String password;

    /** Default constructor for Gson. */
    public RegisterRequest() {
    }

    /**
     * Creates a full registration request.
     *
     * @param nic      National Identity Card number (primary key).
     * @param fullName Prosumer's full name.
     * @param email    Email address.
     * @param phone    Phone number.
     * @param address  Residential address.
     * @param password Account password.
     */
    public RegisterRequest(String nic, String fullName, String email,
                           String phone, String address, String password) {
        this.nic = nic;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
    }

    // Getters and Setters

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
