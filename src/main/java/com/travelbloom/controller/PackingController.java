package com.travelbloom.controller;

import com.travelbloom.dao.PackingDAO;
import com.travelbloom.model.PackingItem;
import com.travelbloom.model.Trip;
import com.travelbloom.service.PackingSuggestionService;
import com.travelbloom.util.BackgroundPhotoUtil;
import com.travelbloom.util.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class PackingController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField quantityField;

    @FXML private TableView<PackingItem> packingTable;
    @FXML private TableColumn<PackingItem, String> itemColumn;
    @FXML private TableColumn<PackingItem, String> categoryColumn;
    @FXML private TableColumn<PackingItem, Integer> quantityColumn;
    @FXML private TableColumn<PackingItem, String> statusColumn;

    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;

    // Reminder toast (top-right), shows 1 min / hides 30s on repeat
    @FXML private VBox suggestionPopup;
    @FXML private VBox suggestionList;

    private final PackingDAO packingDAO = new PackingDAO();
    private final PackingSuggestionService suggestionService = new PackingSuggestionService();

    private final ObservableList<PackingItem> packingList = FXCollections.observableArrayList();

    private Trip currentTrip;
    private PackingItem selectedItem = null;

    private Timeline reminderTimeline;
    private boolean reminderDismissedPermanently = false;


    @FXML
    private void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        categoryComboBox.getItems().addAll(
                "Clothing", "Toiletries", "Electronics", "Documents", "Medication", "Other"
        );

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));

        packingTable.setItems(packingList);

        packingTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedItem = newVal;
                    if (newVal != null) fillForm(newVal);
                });

        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
            suggestionPopup.setManaged(false);
        }
    }


    public void setTrip(Trip trip) {

        this.currentTrip = trip;

        loadItems();
        fetchSmartSuggestions();
        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, trip.getDestination());
    }


    private void loadItems() {

        if (currentTrip == null) return;

        packingList.clear();
        packingList.addAll(packingDAO.getItemsByTripId(currentTrip.getId()));

        updateProgress();
    }

    private void updateProgress() {

        if (currentTrip == null) return;

        int total = packingDAO.getTotalCount(currentTrip.getId());
        int packed = packingDAO.getPackedCount(currentTrip.getId());

        progressLabel.setText(packed + " of " + total + " items packed");
        progressBar.setProgress(total == 0 ? 0 : (double) packed / total);
    }


    // ==============================
    // SMART PACKING SUGGESTIONS +
    // 1-minute-show / 30-second-hide repeating reminder
    // ==============================

    private void fetchSmartSuggestions() {

        if (currentTrip == null || suggestionPopup == null) return;

        Thread thread = new Thread(() -> {

            try {

                List<String> suggestions = suggestionService.suggest(currentTrip);

                List<String> existingNames = packingList.stream()
                        .map(PackingItem::getItemName)
                        .map(String::toLowerCase)
                        .toList();

                List<String> filtered = suggestions.stream()
                        .filter(s -> existingNames.stream().noneMatch(
                                name -> s.toLowerCase().contains(name)))
                        .toList();

                if (!filtered.isEmpty()) {
                    Platform.runLater(() -> {
                        populateSuggestionList(filtered);
                        startReminderCycle();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void populateSuggestionList(List<String> suggestions) {

        suggestionList.getChildren().clear();

        for (String suggestion : suggestions) {
            Label label = new Label("• " + suggestion);
            label.getStyleClass().add("reminder-toast-item");
            label.setWrapText(true);
            suggestionList.getChildren().add(label);
        }
    }

    /**
     * Shows the reminder for 60s, hides it for 30s, and repeats forever
     * (until the user dismisses it with the ✕ button) — exactly the
     * cadence requested: "1 min show, 30 sec hide, then show again".
     */
    private void startReminderCycle() {

        if (reminderTimeline != null) {
            reminderTimeline.stop();
        }

        reminderTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> showReminder()),
                new KeyFrame(Duration.seconds(60), e -> hideReminder()),
                new KeyFrame(Duration.seconds(90)) // marks total cycle length; loops back to 0
        );

        reminderTimeline.setCycleCount(Timeline.INDEFINITE);
        reminderTimeline.play();
    }

    private void showReminder() {
        if (reminderDismissedPermanently) return;
        suggestionPopup.setVisible(true);
        suggestionPopup.setManaged(true);
    }

    private void hideReminder() {
        suggestionPopup.setVisible(false);
        suggestionPopup.setManaged(false);
    }

    @FXML
    private void handleDismissSuggestions() {
        reminderDismissedPermanently = true;
        if (reminderTimeline != null) {
            reminderTimeline.stop();
        }
        hideReminder();
    }

    @FXML
    private void handleAddAllSuggestions() {

        if (currentTrip == null) return;

        for (var node : suggestionList.getChildren()) {

            if (node instanceof Label label) {

                String text = label.getText().replaceFirst("^•\\s*", "").trim();

                PackingItem item = new PackingItem(
                        currentTrip.getId(), text, "Other", 1, false
                );

                packingDAO.addItem(item);
            }
        }

        loadItems();
        handleDismissSuggestions();
    }


    // ==============================
    // ADD / UPDATE / TOGGLE / DELETE (unchanged logic)
    // ==============================

    @FXML
    private void handleAddItem() {

        if (currentTrip == null) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Please open this page from a trip's details first.");
            return;
        }

        String name = itemNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Item", "Please enter an item name.");
            return;
        }

        int quantity = parseQuantity();
        if (quantity < 1) return;

        PackingItem item = new PackingItem(
                currentTrip.getId(),
                name,
                categoryComboBox.getValue() == null ? "Other" : categoryComboBox.getValue(),
                quantity,
                false
        );

        packingDAO.addItem(item);

        loadItems();
        clearForm();
    }

    @FXML
    private void handleUpdateItem() {

        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item first.");
            return;
        }

        String name = itemNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Item", "Please enter an item name.");
            return;
        }

        int quantity = parseQuantity();
        if (quantity < 1) return;

        selectedItem.setItemName(name);
        selectedItem.setCategory(
                categoryComboBox.getValue() == null ? selectedItem.getCategory() : categoryComboBox.getValue()
        );
        selectedItem.setQuantity(quantity);

        packingDAO.updateItem(selectedItem);

        loadItems();
        clearForm();
    }

    @FXML
    private void handleTogglePacked() {

        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item first.");
            return;
        }

        packingDAO.togglePacked(selectedItem.getId(), !selectedItem.isPacked());

        loadItems();
        clearForm();
    }

    @FXML
    private void handleDeleteItem() {

        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item first.");
            return;
        }

        packingDAO.deleteItem(selectedItem.getId());

        loadItems();
        clearForm();
    }


    private void fillForm(PackingItem item) {
        itemNameField.setText(item.getItemName());
        categoryComboBox.setValue(item.getCategory());
        quantityField.setText(String.valueOf(item.getQuantity()));
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        itemNameField.clear();
        categoryComboBox.setValue(null);
        quantityField.clear();
        selectedItem = null;
        packingTable.getSelectionModel().clearSelection();
    }

    private int parseQuantity() {

        String text = quantityField.getText().trim();

        if (text.isEmpty()) return 1;

        try {

            int quantity = Integer.parseInt(text);

            if (quantity < 1) {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be at least 1.");
                return -1;
            }

            return quantity;

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Please enter a whole number for quantity.");
            return -1;
        }
    }


    @FXML
    private void handleBack() {

        Stage stage = (Stage) packingTable.getScene().getWindow();

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
