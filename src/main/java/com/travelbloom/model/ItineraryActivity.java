package com.travelbloom.model;

public class ItineraryActivity {

    private int id;
    private int tripId;

    private String dayLabel;
    private String time;
    private String title;
    private String location;
    private String notes;


    // ==============================
    // EMPTY CONSTRUCTOR
    // ==============================

    public ItineraryActivity() {
    }


    // ==============================
    // CONSTRUCTOR WITHOUT ID
    // ==============================

    public ItineraryActivity(
            int tripId,
            String dayLabel,
            String time,
            String title,
            String location,
            String notes
    ) {

        this.tripId = tripId;
        this.dayLabel = dayLabel;
        this.time = time;
        this.title = title;
        this.location = location;
        this.notes = notes;
    }


    // ==============================
    // CONSTRUCTOR WITH ID
    // ==============================

    public ItineraryActivity(
            int id,
            int tripId,
            String dayLabel,
            String time,
            String title,
            String location,
            String notes
    ) {

        this.id = id;
        this.tripId = tripId;
        this.dayLabel = dayLabel;
        this.time = time;
        this.title = title;
        this.location = location;
        this.notes = notes;
    }


    // ==============================
    // GET ID
    // ==============================

    public int getId() {
        return id;
    }


    // ==============================
    // SET ID
    // ==============================

    public void setId(int id) {
        this.id = id;
    }


    // ==============================
    // GET TRIP ID
    // ==============================

    public int getTripId() {
        return tripId;
    }


    // ==============================
    // SET TRIP ID
    // ==============================

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }


    // ==============================
    // GET DAY LABEL
    // ==============================

    public String getDayLabel() {
        return dayLabel;
    }


    // ==============================
    // SET DAY LABEL
    // ==============================

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }


    // ==============================
    // GET TIME
    // ==============================

    public String getTime() {
        return time;
    }


    // ==============================
    // SET TIME
    // ==============================

    public void setTime(String time) {
        this.time = time;
    }


    // ==============================
    // GET TITLE
    // ==============================

    public String getTitle() {
        return title;
    }


    // ==============================
    // SET TITLE
    // ==============================

    public void setTitle(String title) {
        this.title = title;
    }


    // ==============================
    // GET LOCATION
    // ==============================

    public String getLocation() {
        return location;
    }


    // ==============================
    // SET LOCATION
    // ==============================

    public void setLocation(String location) {
        this.location = location;
    }


    // ==============================
    // GET NOTES
    // ==============================

    public String getNotes() {
        return notes;
    }


    // ==============================
    // SET NOTES
    // ==============================

    public void setNotes(String notes) {
        this.notes = notes;
    }
}