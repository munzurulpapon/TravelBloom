package com.travelbloom.controller;

import com.travelbloom.service.TripPlanReportService;
import com.travelbloom.util.BackgroundPhotoUtil;
import com.travelbloom.util.SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

/**
 * A real conversational flow (no external AI/API call needed for the
 * conversation logic itself — it's a small deterministic state machine),
 * but the FINAL answer is built from genuinely live data:
 *   - weather: Open-Meteo (free, via WeatherService/LocationService)
 *   - famous places: Wikipedia GeoSearch (free, via PlacesService)
 *   - shopping tips: rule-based on destination keywords (honest, like
 *     PriceEstimatorService elsewhere in the app)
 *
 * Flow: destination → number of days → who you're travelling with →
 * full report (itinerary + weather + places + shopping).
 */
public class ChatBotController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private VBox chatContainer;
    @FXML private javafx.scene.control.ScrollPane chatScrollPane;
    @FXML private TextField chatInputField;

    private final TripPlanReportService reportService = new TripPlanReportService();

    // Conversation state
    private enum Step { ASK_DESTINATION, ASK_DAYS, ASK_COMPANION, DONE }

    private Step currentStep;
    private String destination;
    private int days;
    private String companionType;


    @FXML
    private void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        startConversation();
    }


    private void startConversation() {

        chatContainer.getChildren().clear();

        destination = null;
        days = 0;
        companionType = null;

        chatInputField.setDisable(false);
        chatInputField.clear();

        addBotMessage("Hi! 👋 Let's plan your trip. Where would you like to go?");

        currentStep = Step.ASK_DESTINATION;
    }


    // ==============================
    // FREE-TEXT INPUT HANDLING
    // ==============================

    @FXML
    private void handleSendText() {

        String text = chatInputField.getText().trim();

        if (text.isEmpty()) return;

        chatInputField.clear();

        switch (currentStep) {

            case ASK_DESTINATION -> handleDestinationAnswer(text);
            case ASK_DAYS -> handleDaysAnswer(text);

            default -> {
                // Ignore free text while waiting on a quick-reply step
            }
        }
    }

    private void handleDestinationAnswer(String text) {

        destination = text;
        addUserMessage(text);

        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, destination);

        addBotMessage("Nice! How many days is the trip?");
        currentStep = Step.ASK_DAYS;
    }

    private void handleDaysAnswer(String text) {

        addUserMessage(text);

        int parsedDays;

        try {
            parsedDays = Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            parsedDays = 0;
        }

        if (parsedDays < 1) {
            addBotMessage("Please enter the number of days as a number, like \"5\".");
            return; // stay on ASK_DAYS
        }

        days = parsedDays;

        addBotMessage("Got it — " + days + " day(s). Who are you travelling with?");

        currentStep = Step.ASK_COMPANION;
        chatInputField.setDisable(true);

        addQuickReplies(
                List.of("👫 Friends", "👨‍👩‍👧 Family", "💍 Honeymoon", "🧳 Solo"),
                this::handleCompanionAnswer
        );
    }

    private void handleCompanionAnswer(String choice) {

        addUserMessage(choice);
        chatInputField.setDisable(false);

        companionType = choice.replaceAll("^[^ ]+ ", ""); // strip emoji

        addBotMessage("Great — building your " + destination + " plan now... 🧭");

        currentStep = Step.DONE;

        generateReport();
    }


    // ==============================
    // REPORT GENERATION (background thread — live weather + places)
    // ==============================

    private void generateReport() {

        String dest = destination;
        int tripDays = days;
        String companion = companionType;

        Thread thread = new Thread(() -> {

            try {

                TripPlanReportService.TripPlanReport report =
                        reportService.generateReport(dest, tripDays, companion);

                Platform.runLater(() -> displayReport(report));

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() -> addBotMessage(
                        "Sorry, I couldn't find that location — could you try a different spelling or a nearby major city?"
                ));

                Platform.runLater(() -> {
                    currentStep = Step.ASK_DESTINATION;
                    addBotMessage("Where would you like to go?");
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void displayReport(TripPlanReportService.TripPlanReport report) {

        addBotMessage("🌤 Weather right now: " + report.weatherSummary);

        StringBuilder itineraryText = new StringBuilder("📅 Here's your itinerary:\n");
        for (String line : report.itineraryDays) {
            itineraryText.append("• ").append(line).append("\n");
        }
        addBotMessage(itineraryText.toString().trim());

        if (!report.famousPlaces.isEmpty()) {
            StringBuilder placesText = new StringBuilder("📍 Famous places nearby:\n");
            for (String place : report.famousPlaces) {
                placesText.append("• ").append(place).append("\n");
            }
            addBotMessage(placesText.toString().trim());
        }

        if (!report.shoppingTips.isEmpty()) {
            StringBuilder shoppingText = new StringBuilder("🛍 Shopping tips:\n");
            for (String tip : report.shoppingTips) {
                shoppingText.append("• ").append(tip).append("\n");
            }
            addBotMessage(shoppingText.toString().trim());
        }

        addBotMessage("What would you like to do next?");

        addQuickReplies(
                List.of("✈ View Flight Options", "🏨 See Hotel Deals", "📝 Create This Trip", "🔄 Plan Another Trip"),
                this::handleNextStep
        );
    }

    private void handleNextStep(String choice) {

        addUserMessage(choice);

        if (choice.contains("Flight")) {
            addBotMessage("Opening Transport, where you can add and compare your travel options...");
            navigateTo("/view/transport.fxml", "Transport");

        } else if (choice.contains("Hotel")) {
            addBotMessage("Opening Stay, where you can search hotels for your destination...");
            navigateTo("/view/stay.fxml", "Stay");

        } else if (choice.contains("Create")) {
            addBotMessage("Let's get your trip created! Opening the trip form now...");
            navigateTo("/view/create-trip.fxml", "Create Trip");

        } else {
            startConversation();
        }
    }


    // ==============================
    // CHAT UI HELPERS
    // ==============================

    private void addBotMessage(String text) {

        Label bubble = new Label(text);
        bubble.getStyleClass().add("chat-bubble-bot");
        bubble.setWrapText(true);
        bubble.setMaxWidth(460);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addUserMessage(String text) {

        Label bubble = new Label(text);
        bubble.getStyleClass().add("chat-bubble-user");
        bubble.setWrapText(true);
        bubble.setMaxWidth(460);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addQuickReplies(List<String> options, Consumer<String> onChoice) {

        HBox optionsRow = new HBox(10);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        for (String option : options) {

            Button button = new Button(option);
            button.getStyleClass().add("chat-option-button");

            button.setOnAction(e -> {
                chatContainer.getChildren().remove(optionsRow);
                onChoice.accept(option);
            });

            optionsRow.getChildren().add(button);
        }

        chatContainer.getChildren().add(optionsRow);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }


    @FXML
    private void handleRestart() {
        startConversation();
    }


    @FXML
    private void handleBack() {
        navigateTo("/view/smart-planner.fxml", "Smart Planner");
    }


    private void navigateTo(String fxmlPath, String title) {
        Stage stage = (Stage) chatContainer.getScene().getWindow();
        SceneManager.switchScene(stage, fxmlPath, "TravelBloom - " + title);
    }
}
