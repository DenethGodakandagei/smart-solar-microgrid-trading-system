/*
 * Smart Solar Microgrid Trading System
 * BookingSummary.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for the summary displayed after a booking action (create/update/cancel)
 * and for operator verification results.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Summary data shown after a booking action or operator verification.
 * Provides a confirmation with action type, status, and key details.
 */
public class BookingSummary {

    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("status")
    private String status;

    @SerializedName("nodeName")
    private String nodeName;

    @SerializedName("slotDate")
    private String slotDate;

    @SerializedName("slotTime")
    private String slotTime;

    @SerializedName("energyKwh")
    private double energyKwh;

    @SerializedName("prosumerNic")
    private String prosumerNic;

    @SerializedName("prosumerName")
    private String prosumerName;

    @SerializedName("message")
    private String message;

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("timestamp")
    private String timestamp;

    /** Default constructor for Gson. */
    public BookingSummary() {
    }

    // Getters and Setters

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(String slotDate) {
        this.slotDate = slotDate;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(double energyKwh) {
        this.energyKwh = energyKwh;
    }

    public String getProsumerNic() {
        return prosumerNic;
    }

    public void setProsumerNic(String prosumerNic) {
        this.prosumerNic = prosumerNic;
    }

    public String getProsumerName() {
        return prosumerName;
    }

    public void setProsumerName(String prosumerName) {
        this.prosumerName = prosumerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
