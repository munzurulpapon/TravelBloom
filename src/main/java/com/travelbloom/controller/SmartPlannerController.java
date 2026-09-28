package com.travelbloom.controller;

import com.travelbloom.dao.TripDAO;
import com.travelbloom.model.Trip;
import com.travelbloom.service.SmartPlannerService;
import com.travelbloom.util.SceneManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class SmartPlannerController {

    @FXML private ListView<Trip> tripListView;
    @FXML private VBox insightsContainer;
    @FXML private Label selectedTripLabel;

    private final TripDAO tripDAO = new TripDAO();
    private final SmartPlannerService plannerService = new SmartPlannerService();

    private final ObservableList<Trip> trips = FXCollections.observableArrayList();


    @FXML
    private void initialize() {

        trips.addAll(tripDAO.getAllTrips());

        tripListView.setItems(trips);

        tripListView.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Trip trip, boolean empty) {

                super.updateItem(trip, empty);

                if (empty || trip == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(trip.getTitle() + "  —  " + trip.getDestination());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #e6eef4;");
                }
            }
        });

        tripListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        showInsightsFor(newVal);
                    }
                });

        if (!trips.isEmpty()) {
            tripListView.getSelectionModel().selectFirst();
        } else {
            selectedTripLabel.setText("No trips yet — create one first.");
        }
    }


    public void setTrip(Trip trip) {

        if (trip == null) return;

        for (Trip t : trips) {
            if (t.getId() == trip.getId()) {
                tripListView.getSelectionModel().select(t);
                return;
            }
        }
    }


    private void showInsightsFor(Trip trip) {

        selectedTripLabel.setText("🧠 Smart Analysis — " + trip.getTitle());

        insightsContainer.getChildren().clear();

        List<String> insights = plannerService.generateInsights(trip);

        for (String insight : insights) {

            Label label = new Label(insight);
            label.setWrapText(true);
            label.getStyleClass().add("dark-glass-card");
            label.setStyle(
                    label.getStyle()
                            + "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14;"
            );
            label.setMaxWidth(Double.MAX_VALUE);

            insightsContainer.getChildren().add(label);
        }
    }


    @FXML
    private void handleOpenChat() {
        Stage stage = (Stage) tripListView.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/chatbot.fxml", "TravelBloom - Trip Assistant");
    }


    @FXML
    private void handleBack() {
        Stage stage = (Stage) tripListView.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/dashboard.fxml", "TravelBloom - Smart Trip Planner");
    }
}
