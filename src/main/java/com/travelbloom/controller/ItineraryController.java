package com.travelbloom.controller;

import com.travelbloom.dao.ItineraryDAO;
import com.travelbloom.dao.TripDAO;
import com.travelbloom.model.ItineraryActivity;
import com.travelbloom.model.Trip;
import com.travelbloom.util.BackgroundPhotoUtil;
import com.travelbloom.util.SceneManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ItineraryController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    @FXML private TextField dayField;
    @FXML private TextField timeField;
    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private TextField notesField;

    @FXML private TableView<ItineraryActivity> itineraryTable;
    @FXML private TableColumn<ItineraryActivity, String> dayColumn;
    @FXML private TableColumn<ItineraryActivity, String> timeColumn;
    @FXML private TableColumn<ItineraryActivity, String> titleColumn;
    @FXML private TableColumn<ItineraryActivity, String> locationColumn;
    @FXML private TableColumn<ItineraryActivity, String> notesColumn;

    @FXML private Label itineraryTitleLabel;
    @FXML private Label formTitleLabel;

    @FXML private Button backButton;
    @FXML private Button addActivityButton;
    @FXML private Button updateActivityButton;
    @FXML private Button cancelEditButton;

    private final ItineraryDAO itineraryDAO = new ItineraryDAO();
    private final TripDAO tripDAO = new TripDAO();

    private int tripId;
    private String tripTitle;
    private Trip currentTrip;

    private ItineraryActivity editingActivity = null;


    public void setTripId(int tripId) {
        this.tripId = tripId;
        loadActivities();
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
        if (itineraryTitleLabel != null) {
            itineraryTitleLabel.setText("Itinerary — " + tripTitle);
        }
    }

    public void setTrip(Trip trip) {
        this.currentTrip = trip;
        setTripId(trip.getId());
        setTripTitle(trip.getTitle());
        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, trip.getDestination());
    }


    @FXML
    private void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        dayColumn.setCellValueFactory(new PropertyValueFactory<>("dayLabel"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        itineraryTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue != null) {

                        editingActivity = newValue;

                        dayField.setText(newValue.getDayLabel());
                        timeField.setText(newValue.getTime());
                        titleField.setText(newValue.getTitle());
                        locationField.setText(newValue.getLocation());
                        notesField.setText(newValue.getNotes());

                        setEditMode();
                    }
                });

        setAddMode();

        // If this page was opened directly from the sidebar (no trip
        // passed in via setTrip/setTripId), ask the user to pick one
        // instead of silently showing an empty page.
        if (currentTrip == null) {
            Platform.runLater(this::showTripPicker);
        }
    }


    // ==============================
    // TRIP PICKER (used when navigated here without a trip context)
    // ==============================

    private void showTripPicker() {

        List<Trip> trips = tripDAO.getAllTrips();

        if (trips.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Trips Yet");
            alert.setHeaderText(null);
            alert.setContentText("You don't have any trips yet. Create one first.");
            alert.showAndWait();

            goToMyTrips();
            return;
        }

        ChoiceDialog<Trip> dialog = new ChoiceDialog<>(trips.get(0), trips);
        dialog.setTitle("Select a Trip");
        dialog.setHeaderText("Which trip's itinerary do you want to view?");
        dialog.setContentText("Trip:");

        Optional<Trip> chosen = dialog.showAndWait();

        if (chosen.isPresent()) {
            setTrip(chosen.get());
        } else {
            goToMyTrips();
        }
    }

    private void goToMyTrips() {
        Stage stage = (Stage) itineraryTable.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/my-trips.fxml", "TravelBloom - My Trips");
    }


    private void loadActivities() {

        ObservableList<ItineraryActivity> activities =
                FXCollections.observableArrayList(
                        itineraryDAO.getActivitiesByTrip(tripId)
                );

        activities.sort(
                Comparator
                        .comparingInt((ItineraryActivity a) -> extractDayNumber(a.getDayLabel()))
                        .thenComparing((ItineraryActivity a) -> extractTime(a.getTime()))
        );

        itineraryTable.setItems(activities);
    }


    private int extractDayNumber(String dayLabel) {

        if (dayLabel == null || dayLabel.isBlank()) {
            return Integer.MAX_VALUE;
        }

        String digits = dayLabel.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }


    private LocalTime extractTime(String timeText) {

        if (timeText == null || timeText.isBlank()) {
            return LocalTime.MAX;
        }

        String time = timeText.trim();

        DateTimeFormatter[] formats = {
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm")
        };

        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalTime.parse(time.toUpperCase(), formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }

        return LocalTime.MAX;
    }


    private void setAddMode() {

        editingActivity = null;

        if (formTitleLabel != null) formTitleLabel.setText("Add New Activity");
        if (addActivityButton != null) { addActivityButton.setVisible(true); addActivityButton.setManaged(true); }
        if (updateActivityButton != null) { updateActivityButton.setVisible(false); updateActivityButton.setManaged(false); }
        if (cancelEditButton != null) { cancelEditButton.setVisible(false); cancelEditButton.setManaged(false); }
    }


    private void setEditMode() {

        if (formTitleLabel != null) formTitleLabel.setText("Edit Activity");
        if (addActivityButton != null) { addActivityButton.setVisible(false); addActivityButton.setManaged(false); }
        if (updateActivityButton != null) { updateActivityButton.setVisible(true); updateActivityButton.setManaged(true); }
        if (cancelEditButton != null) { cancelEditButton.setVisible(true); cancelEditButton.setManaged(true); }
    }


    private boolean validateFields() {

        String day = dayField.getText().trim();
        String time = timeField.getText().trim();
        String title = titleField.getText().trim();

        if (day.isEmpty()) {
            showWarning("Missing Day", "Please enter the day of the activity.");
            dayField.requestFocus();
            return false;
        }

        if (time.isEmpty()) {
            showWarning("Missing Time", "Please enter the time of the activity.");
            timeField.requestFocus();
            return false;
        }

        if (title.isEmpty()) {
            showWarning("Missing Activity", "Please enter an activity name.");
            titleField.requestFocus();
            return false;
        }

        return true;
    }


    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleAddActivity() {

        if (!validateFields()) return;

        ItineraryActivity activity = new ItineraryActivity(
                tripId,
                dayField.getText().trim(),
                timeField.getText().trim(),
                titleField.getText().trim(),
                locationField.getText().trim(),
                notesField.getText().trim()
        );

        itineraryDAO.addActivity(activity);

        loadActivities();
        clearFields();
        setAddMode();

        showSuccess("Activity Added", "Activity has been added successfully.");
    }


    @FXML
    private void handleUpdateActivity() {

        if (editingActivity == null) {
            showWarning("No Activity Selected", "Please select an activity first.");
            return;
        }

        if (!validateFields()) return;

        editingActivity.setDayLabel(dayField.getText().trim());
        editingActivity.setTime(timeField.getText().trim());
        editingActivity.setTitle(titleField.getText().trim());
        editingActivity.setLocation(locationField.getText().trim());
        editingActivity.setNotes(notesField.getText().trim());

        itineraryDAO.updateActivity(editingActivity);

        loadActivities();
        clearFields();
        setAddMode();
        itineraryTable.getSelectionModel().clearSelection();

        showSuccess("Activity Updated", "Activity has been updated successfully.");
    }


    @FXML
    private void handleCancelEdit() {
        clearFields();
        editingActivity = null;
        itineraryTable.getSelectionModel().clearSelection();
        setAddMode();
    }


    @FXML
    private void handleDeleteActivity() {

        ItineraryActivity selected = itineraryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("No Activity Selected", "Please select an activity first.");
            return;
        }

        itineraryDAO.deleteActivity(selected.getId());

        loadActivities();
        clearFields();
        setAddMode();
        itineraryTable.getSelectionModel().clearSelection();

        showSuccess("Activity Deleted", "Activity has been deleted successfully.");
    }


    private void clearFields() {
        dayField.clear();
        timeField.clear();
        titleField.clear();
        locationField.clear();
        notesField.clear();
    }


    @FXML
    private void handleBackToMyTrips() {

        Stage stage = (Stage) backButton.getScene().getWindow();

        if (currentTrip != null) {

            TripDetailsController controller = SceneManager.switchScene(
                    stage, "/view/trip-details.fxml", "TravelBloom - Trip Details"
            );

            if (controller != null) {
                controller.setTrip(currentTrip);
            }

        } else {

            SceneManager.switchScene(stage, "/view/my-trips.fxml", "TravelBloom - My Trips");
        }
    }
}