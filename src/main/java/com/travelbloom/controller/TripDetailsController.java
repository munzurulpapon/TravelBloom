package com.travelbloom.controller;

import com.travelbloom.model.Trip;
import com.travelbloom.service.ImageService;
import com.travelbloom.service.LocationService;
import com.travelbloom.service.WeatherService;
import com.travelbloom.util.SceneManager;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class TripDetailsController {

    @FXML private StackPane rootPane;
    @FXML private StackPane slideshowPane;
    @FXML private ImageView slideImageA;
    @FXML private ImageView slideImageB;

    @FXML private Button backButton;
    @FXML private Button transportButton;
    @FXML private Button itineraryButton;
    @FXML private Button stayButton;
    @FXML private Button expenseButton;
    @FXML private Button packingButton;
    @FXML private Button smartPlannerButton;

    @FXML private Label tripTitle;
    @FXML private Label tripDestination;
    @FXML private Label tripDate;
    @FXML private Label tripBudget;
    @FXML private Label tripDescription;

    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label latitudeLabel;
    @FXML private Label longitudeLabel;

    private final ImageService imageService = new ImageService();

    private Trip currentTrip;
    private Timeline slideshowTimeline;


    @FXML
    private void initialize() {

        // Make the slideshow always fill the whole window, at any size,
        // fullscreen or not — this is what keeps it "adjustable" the way
        // the rest of the redesign needs.
        slideshowPane.prefWidthProperty().bind(rootPane.widthProperty());
        slideshowPane.prefHeightProperty().bind(rootPane.heightProperty());

        slideImageA.fitWidthProperty().bind(rootPane.widthProperty());
        slideImageA.fitHeightProperty().bind(rootPane.heightProperty());
        slideImageB.fitWidthProperty().bind(rootPane.widthProperty());
        slideImageB.fitHeightProperty().bind(rootPane.heightProperty());
    }


    public void setTrip(Trip trip) {

        this.currentTrip = trip;

        tripTitle.setText(trip.getTitle());
        tripDestination.setText("📍 " + trip.getDestination());
        tripDate.setText("📅 " + trip.getStartDate() + "  →  " + trip.getEndDate());
        tripBudget.setText("💰 ৳ " + String.format("%.0f", trip.getBudget()));

        tripDescription.setText(
                trip.getDescription() == null || trip.getDescription().isBlank()
                        ? "No description available."
                        : trip.getDescription()
        );

        loadWeatherData();
        loadSlideshow();
    }


    // ==========================================
    // PHOTO SLIDESHOW (cross-fades every 5s)
    // ==========================================

    private void loadSlideshow() {

        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
        }

        Thread thread = new Thread(() -> {

            List<String> photoUrls = imageService.getPhotos(currentTrip.getDestination(), 5);

            if (photoUrls.isEmpty()) {
                return; // keep the plain dark background — no Pexels key set yet
            }

            // Download every photo's raw bytes ourselves (with a proper
            // User-Agent) BEFORE handing anything to JavaFX's Image class.
            // Pexels' CDN 403s requests made by JavaFX's own bare
            // Image(url) fetcher, which has no User-Agent header.
            List<Image> images = new ArrayList<>();

            for (String url : photoUrls) {

                byte[] bytes = imageService.downloadImageBytes(url);

                if (bytes == null) continue;

                Image image = new Image(new ByteArrayInputStream(bytes));

                if (!image.isError()) {
                    images.add(image);
                } else {
                    System.out.println(
                            "Failed to decode slideshow photo: " + url + " -> " + image.getException()
                    );
                }
            }

            if (images.isEmpty()) {
                return;
            }

            Platform.runLater(() -> startSlideshow(images));
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void startSlideshow(List<Image> images) {

        // Load the first photo immediately into A
        slideImageA.setImage(images.get(0));
        slideImageA.setOpacity(1.0);
        slideImageB.setOpacity(0.0);

        if (images.size() <= 1) {
            return; // nothing to cross-fade to
        }

        int[] index = {0};
        ImageView[] visible = {slideImageA};
        ImageView[] hidden = {slideImageB};

        slideshowTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {

                    index[0] = (index[0] + 1) % images.size();

                    ImageView incoming = hidden[0];
                    ImageView outgoing = visible[0];

                    incoming.setImage(images.get(index[0]));
                    incoming.setOpacity(0.0);

                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), incoming);
                    fadeIn.setToValue(1.0);

                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), outgoing);
                    fadeOut.setToValue(0.0);

                    fadeIn.play();
                    fadeOut.play();

                    // swap which ImageView is "visible" for next cycle
                    ImageView temp = visible[0];
                    visible[0] = hidden[0];
                    hidden[0] = temp;
                })
        );

        slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
        slideshowTimeline.play();
    }


    // ==========================================
    // WEATHER (unchanged logic)
    // ==========================================

    private void loadWeatherData() {

        if (currentTrip == null) return;

        String destination = currentTrip.getDestination();

        javafx.concurrent.Task<Void> weatherTask = new javafx.concurrent.Task<>() {

            @Override
            protected Void call() {

                try {

                    LocationService locationService = new LocationService();
                    WeatherService weatherService = new WeatherService();

                    var location = locationService.getLocation(destination);

                    if (location == null) {
                        Platform.runLater(() -> conditionLabel.setText("Location not found"));
                        return null;
                    }

                    var weather = weatherService.getWeather(
                            location.getLatitude(), location.getLongitude()
                    );

                    if (weather == null) {
                        Platform.runLater(() -> conditionLabel.setText("Weather unavailable"));
                        return null;
                    }

                    Platform.runLater(() -> {
                        temperatureLabel.setText(weather.getTemperature() + " °C");
                        conditionLabel.setText(getWeatherCondition(weather.getWeatherCode()));
                        windLabel.setText(weather.getWindSpeed() + " km/h");
                        latitudeLabel.setText(String.valueOf(location.getLatitude()));
                        longitudeLabel.setText(String.valueOf(location.getLongitude()));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> conditionLabel.setText("Weather unavailable"));
                }

                return null;
            }
        };

        Thread thread = new Thread(weatherTask);
        thread.setDaemon(true);
        thread.start();
    }


    private String getWeatherCondition(int code) {

        return switch (code) {
            case 0 -> "Clear Sky";
            case 1, 2, 3 -> "Partly Cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55, 56, 57 -> "Drizzle";
            case 61, 63, 65, 66, 67 -> "Rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain Showers";
            case 85, 86 -> "Snow Showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with Hail";
            default -> "Unknown Weather";
        };
    }


    // ==========================================
    // NAVIGATION (now via SceneManager — keeps
    // fullscreen/maximized/size state intact)
    // ==========================================

    @FXML
    private void handleOpenTransport() {
        openChildPage("/view/transport.fxml", "Transport", TransportController.class);
    }

    @FXML
    private void handleOpenItinerary() {
        openChildPage("/view/itinerary.fxml", "Itinerary", ItineraryController.class);
    }

    @FXML
    private void handleOpenStay() {
        openChildPage("/view/stay.fxml", "Stay", StayController.class);
    }

    @FXML
    private void handleOpenExpense() {
        openChildPage("/view/expense.fxml", "Expenses", ExpenseController.class);
    }

    @FXML
    private void handleOpenPacking() {
        openChildPage("/view/packing.fxml", "Packing", PackingController.class);
    }

    @FXML
    private void handleOpenSmartPlanner() {
        openChildPage("/view/smart-planner.fxml", "Smart Planner", SmartPlannerController.class);
    }

    private <T> void openChildPage(String fxml, String title, Class<T> controllerType) {

        if (currentTrip == null) return;

        Stage stage = (Stage) backButton.getScene().getWindow();

        Object controller = SceneManager.switchScene(stage, fxml, "TravelBloom - " + title);

        if (controllerType.isInstance(controller)) {
            try {
                controllerType.getMethod("setTrip", Trip.class).invoke(controller, currentTrip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/my-trips.fxml", "TravelBloom - My Trips");
    }
}