package com.travelbloom.model;

public class Transport {

    // ==============================
    // VARIABLES
    // ==============================

    private int id;

    private int tripId;

    private String type;

    private String fromLocation;

    private String toLocation;

    private String transportDate;

    private String transportTime;

    private double cost;

    private String notes;


    // ==============================
    // CONSTRUCTOR
    // ==============================

    public Transport(
            int tripId,
            String type,
            String fromLocation,
            String toLocation,
            String transportDate,
            String transportTime,
            double cost,
            String notes
    ) {

        this.tripId = tripId;

        this.type = type;

        this.fromLocation = fromLocation;

        this.toLocation = toLocation;

        this.transportDate = transportDate;

        this.transportTime = transportTime;

        this.cost = cost;

        this.notes = notes;
    }


    // ==============================
    // CONSTRUCTOR WITH ID
    // ==============================

    public Transport(
            int id,
            int tripId,
            String type,
            String fromLocation,
            String toLocation,
            String transportDate,
            String transportTime,
            double cost,
            String notes
    ) {

        this.id = id;

        this.tripId = tripId;

        this.type = type;

        this.fromLocation = fromLocation;

        this.toLocation = toLocation;

        this.transportDate = transportDate;

        this.transportTime = transportTime;

        this.cost = cost;

        this.notes = notes;
    }


    // ==============================
    // GETTERS
    // ==============================

    public int getId() {
        return id;
    }


    public int getTripId() {
        return tripId;
    }


    public String getType() {
        return type;
    }


    public String getFromLocation() {
        return fromLocation;
    }


    public String getToLocation() {
        return toLocation;
    }


    public String getTransportDate() {
        return transportDate;
    }


    public String getTransportTime() {
        return transportTime;
    }


    public double getCost() {
        return cost;
    }


    public String getNotes() {
        return notes;
    }


    // ==============================
    // SETTERS
    // ==============================

    public void setId(int id) {
        this.id = id;
    }


    public void setTripId(int tripId) {
        this.tripId = tripId;
    }


    public void setType(String type) {
        this.type = type;
    }


    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }


    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }


    public void setTransportDate(String transportDate) {
        this.transportDate = transportDate;
    }


    public void setTransportTime(String transportTime) {
        this.transportTime = transportTime;
    }


    public void setCost(double cost) {
        this.cost = cost;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }
}