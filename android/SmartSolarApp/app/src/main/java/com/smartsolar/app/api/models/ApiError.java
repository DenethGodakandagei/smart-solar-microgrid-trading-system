/*
 * Smart Solar Microgrid Trading System
 * ApiError.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for parsing error responses from the C# Web API.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Standard error response format from the API.
 * Used to extract meaningful error messages for UI display.
 */
public class ApiError {

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("details")
    private String details;

    /** Default constructor for Gson. */
    public ApiError() {
    }

    // Getters and Setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Returns the most informative error message available.
     *
     * @return Error message string, or a default message.
     */
    public String getDisplayMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (error != null && !error.isEmpty()) {
            return error;
        }
        if (details != null && !details.isEmpty()) {
            return details;
        }
        return "An unexpected error occurred.";
    }
}
