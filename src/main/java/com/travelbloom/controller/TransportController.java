package com.travelbloom.controller;

import com.travelbloom.dao.TransportDAO;
import com.travelbloom.model.Transport;
import com.travelbloom.model.Trip;
import com.travelbloom.service.TransportRouteService;
import com.travelbloom.util.BackgroundPhotoUtil;
import com.travelbloom.util.SceneManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class TransportController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    @FXML private TextField routeFromField;
    @FXML private TextField routeToField;
    @FXML private VBox routeResultsContainer;

    @FXML private ComboBox<String> transportTypeComboBox;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;

    @FXML private Button backButton;

    @FXML private TableView<Transport> transportTable;
    @FXML private TableColumn<Transport, String> typeColumn;
    @FXML private TableColumn<Transport, String> fromColumn;
    @FXML private TableColumn<Transport, String> toColumn;
    @FXML private TableColumn<Transport, String> dateColumn;
    @FXML private TableColumn<Transport, String> timeColumn;
    @FXML private TableColumn<Transport, Double> costColumn;
    @FXML private TableColumn<Transport, String> notesColumn;

    private final TransportDAO transportDAO = new TransportDAO();
    private final TransportRouteService routeService = new TransportRouteService();

    private final ObservableList<Transport> transportList = FXCollections.observableArrayList();

    private Trip currentTrip;
    private int currentTripId = -1;


    @FXML
    public void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        transportTypeComboBox.getItems().addAll(
                "Bus", "Train", "Flight", "Car", "Taxi", "Rickshaw", "Boat", "Other"
        );

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromLocation"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("toLocation"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transportDate"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("transportTime"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        transportTable.setItems(transportList);
    }


    public void setTrip(Trip trip) {
        this.currentTrip = trip;
        this.currentTripId = trip.getId();
        loadTransportData();

        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, trip.getDestination());

        // Pre-fill the route finder with this trip's destination as "To"
        routeToField.setText(trip.getDestination());
    }

    public void setTripId(int tripId) {
        this.currentTripId = tripId;
        this.currentTrip = null;
        loadTransportData();
    }


    private void loadTransportData() {

        if (currentTripId == -1) {
            transportList.clear();
            return;
        }

        transportList.clear();
        transportList.addAll(transportDAO.getTransportByTripId(currentTripId));
        transportTable.setItems(transportList);
    }


    // ==========================================
    // ROUTE FINDER (estimate-based — see
    // TransportRouteService for the live-API upgrade path)
    // ==========================================

    @FXML
    private void handleFindRoutes() {

        String from = routeFromField.getText().trim();
        String to = routeToField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter both a starting point and a destination.");
            return;
        }

        routeResultsContainer.getChildren().clear();
        Label loading = new Label("Finding routes...");
        loading.getStyleClass().add("dark-subtitle");
        routeResultsContainer.getChildren().add(loading);

        Thread thread = new Thread(() -> {

            try {

                List<TransportRouteService.TransportOption> options = routeService.findRoutes(from, to);

                javafx.application.Platform.runLater(() -> {

                    routeResultsContainer.getChildren().clear();

                    if (options.isEmpty()) {
                        Label none = new Label("No routes found between these locations.");
                        none.getStyleClass().add("dark-subtitle");
                        routeResultsContainer.getChildren().add(none);
                        return;
                    }

                    for (TransportRouteService.TransportOption option : options) {
                        routeResultsContainer.getChildren().add(buildRouteCard(option, from, to));
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                javafx.application.Platform.runLater(() -> {
                    routeResultsContainer.getChildren().clear();
                    Label error = new Label("Couldn't find routes — check the location names and try again.");
                    error.getStyleClass().add("dark-subtitle");
                    routeResultsContainer.getChildren().add(error);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private VBox buildRouteCard(TransportRouteService.TransportOption option, String from, String to) {

        VBox card = new VBox(6);
        card.getStyleClass().add("result-card");

        Label modeLabel = new Label(option.getMode());
        modeLabel.getStyleClass().add("result-card-title");

        Label durationLabel = new Label(option.getDurationText());
        durationLabel.getStyleClass().add("dark-subtitle");

        Label priceLabel = new Label(option.getPriceText());
        priceLabel.getStyleClass().add("result-card-price");

        Button useButton = new Button("Use This Option");
        useButton.getStyleClass().add("dark-secondary-button");
        useButton.setOnAction(e -> {
            transportTypeComboBox.setValue(option.getMode());
            fromField.setText(from);
            toField.setText(to);
            costField.setText(String.format("%.0f", option.getEstimatedPrice()));
        });

        HBox topRow = new HBox(14, modeLabel, durationLabel);
        Region spacer = new Region();
        HBox bottomRow = new HBox(14, priceLabel, spacer, useButton);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(topRow, bottomRow);

        return card;
    }


    // ==========================================
    // ADD / EDIT / DELETE TRANSPORT (unchanged logic)
    // ==========================================

    @FXML
    private void handleAddTransport() {

        if (currentTripId == -1) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Please select a trip first.");
            return;
        }

        String type = transportTypeComboBox.getValue();
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        String time = timeField.getText().trim();
        String notes = notesArea.getText().trim();

        if (type == null || type.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select transport type.");
            return;
        }
        if (from.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter starting location.");
            return;
        }
        if (to.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter destination.");
            return;
        }
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select transport date.");
            return;
        }
        if (costField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter transport cost.");
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Cost", "Please enter a valid numeric cost.");
            return;
        }

        String date = datePicker.getValue().toString();

        Transport transport = new Transport(currentTripId, type, from, to, date, time, cost, notes);

        transportDAO.saveTransport(transport);

        loadTransportData();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Transport added successfully!");
    }


    @FXML
    private void handleDeleteTransport() {

        Transport selected = transportTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transport record first.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Transport");
        confirmation.setHeaderText("Delete selected transport?");
        confirmation.setContentText("This transport record will be permanently deleted.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            transportDAO.deleteTransport(selected.getId());
            loadTransportData();
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Transport deleted successfully.");
        }
    }


    @FXML
    private void handleEditTransport() {

        Transport selected = transportTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transport record first.");
            return;
        }

        String type = transportTypeComboBox.getValue();
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        String time = timeField.getText().trim();
        String notes = notesArea.getText().trim();

        if (type == null || from.isEmpty() || to.isEmpty()
                || datePicker.getValue() == null || costField.getText().trim().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Incomplete Information", "Please fill in all required fields.");
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Cost", "Please enter a valid numeric cost.");
            return;
        }

        selected.setType(type);
        selected.setFromLocation(from);
        selected.setToLocation(to);
        selected.setTransportDate(datePicker.getValue().toString());
        selected.setTransportTime(time);
        selected.setCost(cost);
        selected.setNotes(notes);

        transportDAO.updateTransport(selected);

        loadTransportData();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Updated", "Transport updated successfully.");
    }


    @FXML
    private void handleTableSelection() {

        Transport selected = transportTable.getSelectionModel().getSelectedItem();

        if (selected == null) return;

        transportTypeComboBox.setValue(selected.getType());
        fromField.setText(selected.getFromLocation());
        toField.setText(selected.getToLocation());
        datePicker.setValue(java.time.LocalDate.parse(selected.getTransportDate()));
        timeField.setText(selected.getTransportTime());
        costField.setText(String.valueOf(selected.getCost()));
        notesArea.setText(selected.getNotes());
    }


    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        transportTypeComboBox.setValue(null);
        fromField.clear();
        toField.clear();
        datePicker.setValue(null);
        timeField.clear();
        costField.clear();
        notesArea.clear();
        transportTable.getSelectionModel().clearSelection();
    }


    @FXML
    private void handleBack() {

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


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
