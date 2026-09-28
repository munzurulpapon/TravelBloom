package com.travelbloom.controller;

import com.travelbloom.dao.StayDAO;
import com.travelbloom.model.Stay;
import com.travelbloom.model.Trip;
import com.travelbloom.service.HotelService;
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

public class StayController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    @FXML private TextField hotelNameField;
    @FXML private TextField locationField;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;

    @FXML private TableView<Stay> stayTable;
    @FXML private TableColumn<Stay, String> hotelNameColumn;
    @FXML private TableColumn<Stay, String> locationColumn;
    @FXML private TableColumn<Stay, String> checkInColumn;
    @FXML private TableColumn<Stay, String> checkOutColumn;
    @FXML private TableColumn<Stay, Double> costColumn;
    @FXML private TableColumn<Stay, String> notesColumn;

    @FXML private ComboBox<Integer> minStarsComboBox;
    @FXML private VBox hotelResultsContainer;

    private final StayDAO stayDAO = new StayDAO();
    private final HotelService hotelService = new HotelService();

    private final ObservableList<Stay> stayList = FXCollections.observableArrayList();

    private Trip currentTrip;
    private int currentTripId = -1;

    @FXML
    public void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        hotelNameColumn.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        stayTable.setItems(stayList);

        if (minStarsComboBox != null) {
            minStarsComboBox.getItems().addAll(5, 4, 3, 2, 1);
            minStarsComboBox.setValue(3);
        }

        stayTable.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {
                Stay selectedStay = stayTable.getSelectionModel().getSelectedItem();
                if (selectedStay != null) {
                    fillForm(selectedStay);
                }
            }
        });
    }

    public void setTrip(Trip trip) {
        this.currentTrip = trip;
        this.currentTripId = trip.getId();
        loadStayData();
        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, trip.getDestination());
    }

    public void setTripId(int tripId) {
        this.currentTripId = tripId;
        this.currentTrip = null;
        loadStayData();
    }

    private void loadStayData() {

        if (currentTripId == -1) return;

        stayList.clear();
        stayList.addAll(stayDAO.getStaysByTripId(currentTripId));
    }

    @FXML
    private void handleAddStay() {

        if (currentTripId == -1) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Please select a trip first.");
            return;
        }

        String hotelName = hotelNameField.getText().trim();
        String location = locationField.getText().trim();

        String checkIn = checkInPicker.getValue() == null ? "" : checkInPicker.getValue().toString();
        String checkOut = checkOutPicker.getValue() == null ? "" : checkOutPicker.getValue().toString();

        String costText = costField.getText().trim();
        String notes = notesArea.getText().trim();

        if (hotelName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter hotel name.");
            return;
        }

        double cost = 0;

        if (!costText.isEmpty()) {
            try {
                cost = Double.parseDouble(costText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Cost", "Please enter a valid number.");
                return;
            }
        }

        Stay stay = new Stay(0, currentTripId, hotelName, location, checkIn, checkOut, cost, notes);

        stayDAO.addStay(stay);

        loadStayData();
        clearForm();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Stay added successfully.");
    }

    @FXML
    private void handleUpdateStay() {

        Stay selectedStay = stayTable.getSelectionModel().getSelectedItem();

        if (selectedStay == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a stay first.");
            return;
        }

        String hotelName = hotelNameField.getText().trim();
        String location = locationField.getText().trim();

        String checkIn = checkInPicker.getValue() == null ? "" : checkInPicker.getValue().toString();
        String checkOut = checkOutPicker.getValue() == null ? "" : checkOutPicker.getValue().toString();

        String costText = costField.getText().trim();
        String notes = notesArea.getText().trim();

        double cost = 0;

        try {
            if (!costText.isEmpty()) {
                cost = Double.parseDouble(costText);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Cost", "Please enter a valid number.");
            return;
        }

        selectedStay.setHotelName(hotelName);
        selectedStay.setLocation(location);
        selectedStay.setCheckIn(checkIn);
        selectedStay.setCheckOut(checkOut);
        selectedStay.setCost(cost);
        selectedStay.setNotes(notes);

        stayDAO.updateStay(selectedStay);

        loadStayData();
        clearForm();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Stay updated successfully.");
    }

    @FXML
    private void handleDeleteStay() {

        Stay selectedStay = stayTable.getSelectionModel().getSelectedItem();

        if (selectedStay == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a stay first.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Stay");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this stay?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            stayDAO.deleteStay(selectedStay.getId());
            loadStayData();
            clearForm();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    // ==============================
    // HOTEL SEARCH (dark result cards)
    // ==============================

    @FXML
    private void handleFindHotels() {

        if (currentTrip == null) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Open this page from a trip's details first.");
            return;
        }

        int minStars = minStarsComboBox.getValue() == null ? 3 : minStarsComboBox.getValue();

        List<HotelService.HotelOption> options =
                hotelService.searchHotels(currentTrip.getDestination(), minStars);

        hotelResultsContainer.getChildren().clear();

        for (HotelService.HotelOption option : options) {
            hotelResultsContainer.getChildren().add(buildHotelCard(option));
        }
    }

    private VBox buildHotelCard(HotelService.HotelOption option) {

        VBox card = new VBox(4);
        card.getStyleClass().add("result-card");

        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().add("result-card-title");

        Label starsLabel = new Label(option.getStarsText());
        starsLabel.setStyle("-fx-text-fill: #f5a623;");

        Label priceLabel = new Label(option.getPriceText());
        priceLabel.getStyleClass().add("result-card-price");

        Button useButton = new Button("Use This Hotel");
        useButton.getStyleClass().add("dark-secondary-button");
        useButton.setOnAction(e -> fillFromHotelSuggestion(
                option.getName(), option.getLocation(), option.getPricePerNight()
        ));

        HBox topRow = new HBox(10, nameLabel, starsLabel);
        Region spacer = new Region();
        HBox bottomRow = new HBox(10, priceLabel, spacer, useButton);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(topRow, bottomRow);

        return card;
    }

    public void fillFromHotelSuggestion(String hotelName, String location, double estimatedCost) {
        hotelNameField.setText(hotelName);
        locationField.setText(location);
        costField.setText(String.format("%.0f", estimatedCost));
    }

    private void fillForm(Stay stay) {

        hotelNameField.setText(stay.getHotelName());
        locationField.setText(stay.getLocation());

        if (!stay.getCheckIn().isEmpty()) {
            checkInPicker.setValue(java.time.LocalDate.parse(stay.getCheckIn()));
        }

        if (!stay.getCheckOut().isEmpty()) {
            checkOutPicker.setValue(java.time.LocalDate.parse(stay.getCheckOut()));
        }

        costField.setText(String.valueOf(stay.getCost()));
        notesArea.setText(stay.getNotes());
    }

    private void clearForm() {
        hotelNameField.clear();
        locationField.clear();
        checkInPicker.setValue(null);
        checkOutPicker.setValue(null);
        costField.clear();
        notesArea.clear();
        stayTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {

        Stage stage = (Stage) stayTable.getScene().getWindow();

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
