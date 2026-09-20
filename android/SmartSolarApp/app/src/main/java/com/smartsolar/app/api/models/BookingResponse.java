/*
 * Smart Solar Microgrid Trading System
 * BookingResponse.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for receiving booking details from the API.
 * Used in booking lists, history, and detail views.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Response from booking CRUD endpoints.
 * Contains full booking details including status, node info, and timestamps.
 */
public class BookingResponse {

    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName("prosumerNic")
    private String prosumerNic;

    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("nodeName")
    private String nodeName;

    @SerializedName("slotDate")
    private String slotDate;

    @SerializedName("slotTime")
    private String slotTime;

    @SerializedName("energyKwh")
    private double energyKwh;

    @SerializedName("status")
    private String status;

    @SerializedName("notes")
    private String notes;

    @SerializedName("qrToken")
    private String qrToken;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("message")
    private String message;

    /** Default constructor for Gson. */
    public BookingResponse() {
    }

    // Getters and Setters

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getProsumerNic() {
        return prosumerNic;
    }

    public void setProsumerNic(String prosumerNic) {
        this.prosumerNic = prosumerNic;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
