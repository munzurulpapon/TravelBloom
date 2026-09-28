package com.travelbloom.model;

public class Expense {

    private int id;
    private int tripId;
    private String category;
    private String description;
    private double amount;
    private String date;


    // ==============================
    // EMPTY CONSTRUCTOR
    // ==============================

    public Expense() {
    }


    // ==============================
    // CONSTRUCTOR WITHOUT ID
    // ==============================

    public Expense(int tripId,
                   String category,
                   String description,
                   double amount,
                   String date) {

        this.tripId = tripId;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }


    // ==============================
    // CONSTRUCTOR WITH ID
    // ==============================

    public Expense(int id,
                   int tripId,
                   String category,
                   String description,
                   double amount,
                   String date) {

        this.id = id;
        this.tripId = tripId;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }


    // ==============================
    // GETTERS / SETTERS
    // ==============================

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}