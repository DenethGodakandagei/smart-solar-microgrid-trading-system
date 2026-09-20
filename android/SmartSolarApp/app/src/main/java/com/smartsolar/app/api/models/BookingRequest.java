/*
 * Smart Solar Microgrid Trading System
 * BookingRequest.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO for creating or updating energy slot reservations.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body sent to POST /api/bookings (create) and PUT /api/bookings/{id} (update).
 * Contains the grid node, slot timing, and energy amount for the reservation.
 */
public class BookingRequest {

    @SerializedName("prosumerNic")
    private String prosumerNic;

    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("slotDate")
    private String slotDate;

    @SerializedName("slotTime")
    private String slotTime;

    @SerializedName("energyKwh")
    private double energyKwh;

    @SerializedName("notes")
    private String notes;

    /** Default constructor for Gson. */
    public BookingRequest() {
    }

    /**
     * Creates a booking request with all required fields.
     *
     * @param prosumerNic The prosumer's NIC.
     * @param nodeId      The target grid node ID.
     * @param slotDate    The booking date (ISO format).
     * @param slotTime    The booking time slot.
     * @param energyKwh   Energy amount in kWh.
     */
    public BookingRequest(String prosumerNic, String nodeId, String slotDate,
                          String slotTime, double energyKwh) {
        this.prosumerNic = prosumerNic;
        this.nodeId = nodeId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.energyKwh = energyKwh;
    }

    // Getters and Setters

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
