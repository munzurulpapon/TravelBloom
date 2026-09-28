package com.travelbloom.model;

public class Trip {

    private int id;
    private String title;
    private String destination;
    private String startDate;
    private String endDate;
    private double budget;
    private String description;


    public Trip() {
    }


    public Trip(String title,
                String destination,
                String startDate,
                String endDate,
                double budget,
                String description) {

        this.title = title;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.description = description;

    }


    public Trip(int id,
                String title,
                String destination,
                String startDate,
                String endDate,
                double budget,
                String description) {

        this.id = id;
        this.title = title;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.description = description;

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }


    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }


    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    // Used by ChoiceDialog<Trip> in the trip-picker (ItineraryController etc.)
    // so the dropdown shows "Title — Destination" instead of object hashcode.
    @Override
    public String toString() {
        return title + " — " + destination;
    }

}