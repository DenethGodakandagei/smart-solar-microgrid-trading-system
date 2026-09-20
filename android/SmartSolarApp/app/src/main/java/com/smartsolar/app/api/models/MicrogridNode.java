/*
 * Smart Solar Microgrid Trading System
 * MicrogridNode.java
 *
 * Member 2 - Native Android Prosumer Application
 * DTO representing a solar microgrid node/hub with GPS coordinates,
 * capacity specifications, and battery storage information.
 */
package com.smartsolar.app.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Microgrid node data from GET /api/nodes.
 * Contains location for Google Maps plotting and capacity for booking display.
 */
public class MicrogridNode {

    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("nodeName")
    private String nodeName;

    @SerializedName("location")
    private String location;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("capacityKwh")
    private double capacityKwh;

    @SerializedName("batterySlots")
    private int batterySlots;

    @SerializedName("availableSlots")
    private int availableSlots;

    @SerializedName("status")
    private String status;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("createdAt")
    private String createdAt;

    /** Default constructor for Gson. */
    public MicrogridNode() {
    }

    // Getters and Setters

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getCapacityKwh() {
        return capacityKwh;
    }

    public void setCapacityKwh(double capacityKwh) {
        this.capacityKwh = capacityKwh;
    }

    public int getBatterySlots() {
        return batterySlots;
    }

    public void setBatterySlots(int batterySlots) {
        this.batterySlots = batterySlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a display-friendly string for node selection spinners.
     */
    @Override
    public String toString() {
        return nodeName + " (" + capacityKwh + " kWh)";
    }
}
