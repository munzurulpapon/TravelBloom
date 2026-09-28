package com.travelbloom.model;

public class PackingItem {

    private int id;
    private int tripId;
    private String itemName;
    private String category;
    private int quantity;
    private boolean packed;


    // ==============================
    // EMPTY CONSTRUCTOR
    // ==============================

    public PackingItem() {
    }


    // ==============================
    // CONSTRUCTOR WITHOUT ID
    // ==============================

    public PackingItem(int tripId,
                       String itemName,
                       String category,
                       int quantity,
                       boolean packed) {

        this.tripId = tripId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.packed = packed;
    }


    // ==============================
    // CONSTRUCTOR WITH ID
    // ==============================

    public PackingItem(int id,
                       int tripId,
                       String itemName,
                       String category,
                       int quantity,
                       boolean packed) {

        this.id = id;
        this.tripId = tripId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.packed = packed;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    // Used by TableView PropertyValueFactory to show "Packed" / "Not Packed"
    public String getStatusText() {
        return packed ? "✔ Packed" : "✘ Not Packed";
    }
}