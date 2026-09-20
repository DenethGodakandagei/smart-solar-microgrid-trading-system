/*
 * Smart Solar Microgrid Trading System
 * DashboardStats.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for receiving dashboard statistics from the API,
 * including pending reservation and approved booking counts.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Dashboard statistics from GET /api/bookings/dashboard.
 * Used to display count cards on the prosumer dashboard screen.
 */
public class DashboardStats {

    @SerializedName("pendingCount")
    private int pendingCount;

    @SerializedName("approvedCount")
    private int approvedCount;

    @SerializedName("completedCount")
    private int completedCount;

    @SerializedName("cancelledCount")
    private int cancelledCount;

    @SerializedName("totalBookings")
    private int totalBookings;

    /** Default constructor for Gson. */
    public DashboardStats() {
    }

    // Getters and Setters

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }
}
