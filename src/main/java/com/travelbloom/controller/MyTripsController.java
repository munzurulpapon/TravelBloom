package com.travelbloom.controller;

import com.travelbloom.dao.TripDAO;
import com.travelbloom.model.Trip;
import com.travelbloom.service.ImageService;
import com.travelbloom.util.SceneManager;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MyTripsController {

    @FXML private TextField searchField;
    @FXML private Button dashboardButton;
    @FXML private Button tripsButton;
    @FXML private Button itineraryButton;
    @FXML private Button transportButton;
    @FXML private Button stayButton;
    @FXML private Button expensesButton;
    @FXML private Button packingButton;
    @FXML private Button smartPlannerButton;
    @FXML private Button createTripButton;
    @FXML private FlowPane tripsContainer;

    private final TripDAO tripDAO = new TripDAO();
    private final ImageService imageService = new ImageService();


    @FXML
    private void initialize() {

        dashboardButton.setOnAction(e -> nav("/view/dashboard.fxml", "Dashboard"));
        itineraryButton.setOnAction(e -> nav("/view/itinerary.fxml", "Itinerary"));
        transportButton.setOnAction(e -> nav("/view/transport.fxml", "Transport"));
        stayButton.setOnAction(e -> nav("/view/stay.fxml", "Stay"));
        expensesButton.setOnAction(e -> nav("/view/expense.fxml", "Expenses"));
        packingButton.setOnAction(e -> nav("/view/packing.fxml", "Packing"));
        smartPlannerButton.setOnAction(e -> nav("/view/smart-planner.fxml", "Smart Planner"));
        createTripButton.setOnAction(e -> nav("/view/create-trip.fxml", "Create Trip"));

        loadTrips();

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> filterTrips(newValue)
        );
    }


    private void nav(String fxml, String title) {
        Stage stage = (Stage) tripsContainer.getScene().getWindow();
        SceneManager.switchScene(stage, fxml, "TravelBloom - " + title);
    }


    private void filterTrips(String searchText) {

        tripsContainer.getChildren().clear();

        List<Trip> trips = tripDAO.getAllTrips();

        String search = (searchText == null ? "" : searchText).trim().toLowerCase();

        int matching = 0;

        for (Trip trip : trips) {

            String title = trip.getTitle() == null ? "" : trip.getTitle().toLowerCase();
            String destination = trip.getDestination() == null ? "" : trip.getDestination().toLowerCase();

            if (title.contains(search) || destination.contains(search)) {
                tripsContainer.getChildren().add(buildTripCard(trip));
                matching++;
            }
        }

        if (matching == 0) {

            Label emptyLabel = new Label(
                    search.isEmpty() ? "No trips found. Create your first trip!" : "No matching trips found."
            );
            emptyLabel.getStyleClass().add("dark-subtitle");
            tripsContainer.getChildren().add(emptyLabel);
        }
    }


    private void loadTrips() {

        tripsContainer.getChildren().clear();

        List<Trip> trips = tripDAO.getAllTrips();

        if (trips.isEmpty()) {
            Label emptyLabel = new Label("No trips found. Create your first trip!");
            emptyLabel.getStyleClass().add("dark-subtitle");
            tripsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Trip trip : trips) {
            tripsContainer.getChildren().add(buildTripCard(trip));
        }
    }


    // ==========================================
    // PHOTO TRIP CARD (matches Dashboard style)
    // ==========================================

    private StackPane buildTripCard(Trip trip) {

        StackPane card = new StackPane();
        card.setPrefSize(320, 260);
        card.setMaxSize(320, 260);
        card.getStyleClass().add("trip-photo-card-fallback");

        // Background ImageView (replaces the old CSS -fx-background-image
        // string, which silently failed — see downloadImageBytes() below
        // for why).
        ImageView bgImageView = new ImageView();
        bgImageView.setFitWidth(320);
        bgImageView.setFitHeight(260);
        bgImageView.setPreserveRatio(false);
        bgImageView.setSmooth(true);

        Rectangle clip = new Rectangle(320, 260);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        bgImageView.setClip(clip);

        VBox overlay = new VBox(6);
        overlay.getStyleClass().add("trip-card-overlay");
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPrefSize(320, 260);
        overlay.setMaxSize(320, 260);

        Label badge = new Label(trip.getDestination());
        badge.getStyleClass().add("trip-card-badge");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label title = new Label(trip.getTitle());
        title.getStyleClass().add("trip-card-title");

        Label meta = new Label(trip.getStartDate() + " → " + trip.getEndDate());
        meta.getStyleClass().add("trip-card-meta");

        Label budget = new Label("Budget: ৳ " + String.format("%.2f", trip.getBudget()));
        budget.getStyleClass().add("trip-card-meta");

        HBox buttons = new HBox(8);

        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("dark-primary-button");
        viewButton.setOnAction(e -> openTripDetails(trip));

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("dark-secondary-button");
        editButton.setOnAction(e -> editTrip(trip));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("dark-danger-button");
        deleteButton.setOnAction(e -> deleteTrip(trip));

        buttons.getChildren().addAll(viewButton, editButton, deleteButton);

        overlay.getChildren().addAll(badge, spacer, title, meta, budget, buttons);

        card.getChildren().addAll(bgImageView, overlay);

        // Fetch the destination photo's bytes ourselves (with a proper
        // User-Agent header) and decode them into an Image — Pexels'
        // CDN returns 403 to JavaFX's own bare Image(url) fetcher.
        Thread thread = new Thread(() -> {

            String photoUrl = imageService.getPhotoUrl(trip.getDestination());

            if (photoUrl == null) return;

            byte[] imageBytes = imageService.downloadImageBytes(photoUrl);

            if (imageBytes == null) return;

            Image image = new Image(new ByteArrayInputStream(imageBytes), 320, 260, false, true);

            if (!image.isError()) {
                Platform.runLater(() -> {
                    bgImageView.setImage(image);
                    card.getStyleClass().remove("trip-photo-card-fallback");
                });
            } else {
                System.out.println(
                        "Failed to decode trip photo: " + photoUrl + " -> " + image.getException()
                );
            }
        });

        thread.setDaemon(true);
        thread.start();

        return card;
    }


    private void openTripDetails(Trip trip) {

        Stage stage = (Stage) tripsContainer.getScene().getWindow();

        TripDetailsController controller = SceneManager.switchScene(
                stage, "/view/trip-details.fxml", "TravelBloom - Trip Details"
        );

        if (controller != null) {
            controller.setTrip(trip);
        }
    }


    private void deleteTrip(Trip trip) {

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Trip");
        confirmation.setHeaderText("Delete \"" + trip.getTitle() + "\"?");
        confirmation.setContentText("This trip will be permanently deleted.");

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {
                tripDAO.deleteTrip(trip.getId());
                filterTrips(searchField.getText());
            }
        });
    }


    private void editTrip(Trip trip) {

        Stage stage = (Stage) tripsContainer.getScene().getWindow();

        CreateTripController controller = SceneManager.switchScene(
                stage, "/view/create-trip.fxml", "TravelBloom - Edit Trip"
        );

        if (controller != null) {
            controller.setTripForEditing(trip);
        }
    }
}