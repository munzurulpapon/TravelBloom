package com.travelbloom.controller;

import com.travelbloom.dao.TripDAO;
import com.travelbloom.model.Trip;
import com.travelbloom.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class CreateTripController {

    @FXML
    private TextField tripTitleField;

    @FXML
    private TextField destinationField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField budgetField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Button backButton;

    @FXML
    private Button createButton;


    // ==============================
    // DATABASE
    // ==============================

    private final TripDAO tripDAO = new TripDAO();


    // ==============================
    // EDIT MODE VARIABLES
    // ==============================

    private Trip editingTrip;

    private boolean editMode = false;


    // ==============================
    // INITIALIZE
    // ==============================

    @FXML
    private void initialize() {

        System.out.println("Create Trip Controller Loaded!");

        backButton.setOnAction(event -> openMyTrips());

        createButton.setOnAction(event -> createTrip());
    }


    // ==============================
    // SET TRIP FOR EDITING
    // ==============================

    public void setTripForEditing(Trip trip) {

        this.editingTrip = trip;

        this.editMode = true;


        tripTitleField.setText(trip.getTitle());

        destinationField.setText(trip.getDestination());

        startDatePicker.setValue(LocalDate.parse(trip.getStartDate()));

        endDatePicker.setValue(LocalDate.parse(trip.getEndDate()));

        budgetField.setText(String.valueOf(trip.getBudget()));

        descriptionField.setText(trip.getDescription());

        createButton.setText("Update Trip");
    }


    // ==============================
    // CREATE / UPDATE TRIP
    // ==============================

    private void createTrip() {

        String title = tripTitleField.getText().trim();
        String destination = destinationField.getText().trim();
        String budgetText = budgetField.getText().trim();
        String description = descriptionField.getText().trim();


        // ==============================
        // VALIDATE REQUIRED FIELDS
        // ==============================

        if (title.isEmpty()
                || destination.isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null
                || budgetText.isEmpty()) {

            showAlert("Missing Information", "Please fill in all required fields.");
            return;
        }


        // ==============================
        // VALIDATE DATES
        // ==============================

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {

            showAlert("Invalid Dates", "End date cannot be before start date.");
            return;
        }


        // ==============================
        // VALIDATE BUDGET
        // ==============================

        double budget;

        try {

            budget = Double.parseDouble(budgetText);

            if (budget < 0) {
                showAlert("Invalid Budget", "Budget cannot be negative.");
                return;
            }

        } catch (NumberFormatException e) {

            showAlert("Invalid Budget", "Please enter a valid numeric budget.");
            return;
        }


        // ==============================
        // UPDATE EXISTING TRIP
        // ==============================

        if (editMode) {

            editingTrip.setTitle(title);
            editingTrip.setDestination(destination);
            editingTrip.setStartDate(startDatePicker.getValue().toString());
            editingTrip.setEndDate(endDatePicker.getValue().toString());
            editingTrip.setBudget(budget);
            editingTrip.setDescription(description);

            tripDAO.updateTrip(editingTrip);

            showAlert("Trip Updated", "Your trip has been updated successfully!");

        }


        // ==============================
        // CREATE NEW TRIP
        // ==============================

        else {

            Trip trip = new Trip(
                    title,
                    destination,
                    startDatePicker.getValue().toString(),
                    endDatePicker.getValue().toString(),
                    budget,
                    description
            );

            tripDAO.saveTrip(trip);

            showAlert("Trip Created", "Your trip has been saved successfully!");
        }


        openMyTrips();
    }


    // ==============================
    // OPEN MY TRIPS  (fixed — uses SceneManager so fullscreen/size state
    // survives, instead of the old raw new Scene(root, 1200, 750))
    // ==============================

    private void openMyTrips() {

        Stage stage = (Stage) backButton.getScene().getWindow();

        SceneManager.switchScene(stage, "/view/my-trips.fxml", "TravelBloom - My Trips");
    }


    // ==============================
    // SHOW ALERT
    // ==============================

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("TravelBloom");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
}