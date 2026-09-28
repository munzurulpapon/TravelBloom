package com.travelbloom.controller;

import com.travelbloom.dao.ExpenseDAO;
import com.travelbloom.dao.StayDAO;
import com.travelbloom.dao.TransportDAO;
import com.travelbloom.dao.TripDAO;
import com.travelbloom.model.Trip;
import com.travelbloom.service.ImageService;
import com.travelbloom.util.Session;
import com.travelbloom.util.SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML private Button dashboardButton;
    @FXML private Button tripsButton;
    @FXML private Button itineraryButton;
    @FXML private Button transportButton;
    @FXML private Button stayButton;
    @FXML private Button expensesButton;
    @FXML private Button packingButton;
    @FXML private Button smartPlannerButton;
    @FXML private Button logoutButton;

    @FXML private Label greetingLabel;
    @FXML private Label myTripsCount;
    @FXML private Label upcomingCount;
    @FXML private Label expensesAmount;

    @FXML private FlowPane tripsFlowPane;

    private final TripDAO tripDAO = new TripDAO();
    private final TransportDAO transportDAO = new TransportDAO();
    private final StayDAO stayDAO = new StayDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final ImageService imageService = new ImageService();


    @FXML
    private void initialize() {

        loadDashboardData();
        buildTripCards();

        tripsButton.setOnAction(e -> nav("/view/my-trips.fxml", "My Trips"));
        itineraryButton.setOnAction(e -> nav("/view/itinerary.fxml", "Itinerary"));
        transportButton.setOnAction(e -> nav("/view/transport.fxml", "Transport"));
        stayButton.setOnAction(e -> nav("/view/stay.fxml", "Stay"));
        expensesButton.setOnAction(e -> nav("/view/expense.fxml", "Expenses"));
        packingButton.setOnAction(e -> nav("/view/packing.fxml", "Packing"));
        smartPlannerButton.setOnAction(e -> nav("/view/smart-planner.fxml", "Smart Planner"));

        if (logoutButton != null) {
            logoutButton.setOnAction(event -> handleLogout());
        }

        if (greetingLabel != null) {
            greetingLabel.setText("Good Morning, " + Session.getUsername() + " 👋");
        }
    }


    private void nav(String fxml, String title) {
        Stage stage = (Stage) tripsFlowPane.getScene().getWindow();
        SceneManager.switchScene(stage, fxml, "TravelBloom - " + title);
    }


    private void handleLogout() {
        Session.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/login.fxml", "TravelBloom - Login");
    }


    // ==========================================
    // LOAD DASHBOARD STATS (unchanged logic)
    // ==========================================

    private void loadDashboardData() {

        List<Trip> trips = tripDAO.getAllTrips();

        myTripsCount.setText(String.format("%02d", trips.size()));

        LocalDate today = LocalDate.now();
        int upcomingTrips = 0;

        for (Trip trip : trips) {
            try {
                LocalDate startDate = LocalDate.parse(trip.getStartDate());
                if (!startDate.isBefore(today)) {
                    upcomingTrips++;
                }
            } catch (Exception ignored) {
                // invalid date, skip
            }
        }

        upcomingCount.setText(String.format("%02d", upcomingTrips));

        double totalSpent = 0;
        for (Trip trip : trips) {
            totalSpent += transportDAO.getTotalTransportCost(trip.getId());
            totalSpent += stayDAO.getTotalStayCostByTripId(trip.getId());
        }
        totalSpent += expenseDAO.getTotalExpensesAll();

        expensesAmount.setText("৳ " + String.format("%.0f", totalSpent));
    }


    // ==========================================
    // TRIP PHOTO CARDS
    // ==========================================

    private void buildTripCards() {

        tripsFlowPane.getChildren().clear();

        List<Trip> trips = tripDAO.getAllTrips();

        if (trips.isEmpty()) {

            Label empty = new Label("No trips yet — create your first one from My Trips.");
            empty.getStyleClass().add("dark-subtitle");
            tripsFlowPane.getChildren().add(empty);
            return;
        }

        // Show the most recent few trips here; the full list lives on My Trips
        int shown = 0;
        for (Trip trip : trips) {

            if (shown >= 4) break;

            tripsFlowPane.getChildren().add(buildTripCard(trip));
            shown++;
        }
    }

    private StackPane buildTripCard(Trip trip) {

        StackPane card = new StackPane();
        card.setPrefSize(340, 220);
        card.setMaxSize(340, 220);
        card.getStyleClass().add("trip-photo-card-fallback"); // shown until/if photo loads

        // Background ImageView (replaces the old CSS -fx-background-image
        // string, which silently failed — see downloadImageBytes() below
        // for why).
        ImageView bgImageView = new ImageView();
        bgImageView.setFitWidth(340);
        bgImageView.setFitHeight(220);
        bgImageView.setPreserveRatio(false);
        bgImageView.setSmooth(true);

        Rectangle clip = new Rectangle(340, 220);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        bgImageView.setClip(clip);

        VBox overlay = new VBox(6);
        overlay.getStyleClass().add("trip-card-overlay");
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPrefSize(340, 220);
        overlay.setMaxSize(340, 220);

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

        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("dark-secondary-button");
        viewButton.setOnAction(e -> openTripDetails(trip));

        overlay.getChildren().addAll(badge, spacer, title, meta, budget, viewButton);

        card.getChildren().addAll(bgImageView, overlay);

        // Fetch the destination photo's bytes ourselves (with a proper
        // User-Agent header) and decode them into an Image — Pexels'
        // CDN returns 403 to JavaFX's own bare Image(url) fetcher.
        Thread thread = new Thread(() -> {

            String photoUrl = imageService.getPhotoUrl(trip.getDestination());

            if (photoUrl == null) return;

            byte[] imageBytes = imageService.downloadImageBytes(photoUrl);

            if (imageBytes == null) return;

            Image image = new Image(new ByteArrayInputStream(imageBytes), 340, 220, false, true);

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

        Stage stage = (Stage) tripsFlowPane.getScene().getWindow();

        TripDetailsController controller = SceneManager.switchScene(
                stage, "/view/trip-details.fxml", "TravelBloom - Trip Details"
        );

        if (controller != null) {
            controller.setTrip(trip);
        }
    }
}