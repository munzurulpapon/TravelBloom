package com.travelbloom.model;

public class Stay {

    private int id;
    private int tripId;
    private String hotelName;
    private String location;
    private String checkIn;
    private String checkOut;
    private double cost;
    private String notes;

    public Stay() {
    }

    public Stay(int id, int tripId, String hotelName, String location,
                String checkIn, String checkOut, double cost, String notes) {

        this.id = id;
        this.tripId = tripId;
        this.hotelName = hotelName;
        this.location = location;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.cost = cost;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}