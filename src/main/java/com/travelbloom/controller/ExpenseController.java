package com.travelbloom.controller;

import com.travelbloom.dao.ExpenseDAO;
import com.travelbloom.model.Expense;
import com.travelbloom.model.Trip;
import com.travelbloom.service.PriceEstimatorService;
import com.travelbloom.util.BackgroundPhotoUtil;
import com.travelbloom.util.SceneManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ExpenseController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;

    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> dateColumn;

    @FXML private Label budgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingLabel;

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final PriceEstimatorService priceEstimatorService = new PriceEstimatorService();

    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    private Trip currentTrip;
    private Expense editingExpense = null;


    @FXML
    private void initialize() {

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        categoryComboBox.getItems().addAll(
                "Food", "Shopping", "Sightseeing", "Activities", "Emergency", "Other"
        );

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        expenseTable.setItems(expenseList);

        expenseTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Expense selected = expenseTable.getSelectionModel().getSelectedItem();
                if (selected != null) fillForm(selected);
            }
        });
    }


    public void setTrip(Trip trip) {
        this.currentTrip = trip;
        loadExpenses();
        BackgroundPhotoUtil.applyDestinationBackground(backgroundImage, trip.getDestination());
    }


    private void loadExpenses() {

        if (currentTrip == null) return;

        expenseList.clear();
        expenseList.addAll(expenseDAO.getExpensesByTripId(currentTrip.getId()));

        updateSummary();
    }

    private void updateSummary() {

        if (currentTrip == null) return;

        double budget = currentTrip.getBudget();
        double spent = expenseDAO.getTotalExpensesByTripId(currentTrip.getId());
        double remaining = budget - spent;

        budgetLabel.setText("৳ " + String.format("%.2f", budget));
        totalSpentLabel.setText("৳ " + String.format("%.2f", spent));
        remainingLabel.setText("৳ " + String.format("%.2f", remaining));

        remainingLabel.setStyle(
                remaining < 0
                        ? "-fx-text-fill: #ff8a80; -fx-font-weight: bold; -fx-font-size: 26px;"
                        : "-fx-text-fill: #6fe3a8; -fx-font-weight: bold; -fx-font-size: 26px;"
        );
    }


    @FXML
    private void handleSuggestPrice() {

        if (currentTrip == null) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Open this page from a trip's details first.");
            return;
        }

        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Pick a Category", "Select a category first so I can estimate a price.");
            return;
        }

        double estimate = priceEstimatorService.estimate(
                currentTrip.getDestination(), categoryComboBox.getValue()
        );

        amountField.setText(String.format("%.0f", estimate));
    }


    @FXML
    private void handleAddExpense() {

        if (currentTrip == null) {
            showAlert(Alert.AlertType.WARNING, "No Trip Selected", "Please open this page from a trip's details first.");
            return;
        }

        if (!validateFields()) return;

        String category = categoryComboBox.getValue();
        String description = descriptionField.getText().trim();
        double amount = Double.parseDouble(amountField.getText().trim());

        String date = datePicker.getValue() == null
                ? LocalDate.now().toString()
                : datePicker.getValue().toString();

        Expense expense = new Expense(currentTrip.getId(), category, description, amount, date);

        expenseDAO.addExpense(expense);

        loadExpenses();
        clearForm();

        showAlert(Alert.AlertType.INFORMATION, "Expense Added", "Your expense has been recorded.");
    }


    @FXML
    private void handleUpdateExpense() {

        if (editingExpense == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an expense first (double-click a row).");
            return;
        }

        if (!validateFields()) return;

        editingExpense.setCategory(categoryComboBox.getValue());
        editingExpense.setDescription(descriptionField.getText().trim());
        editingExpense.setAmount(Double.parseDouble(amountField.getText().trim()));

        editingExpense.setDate(
                datePicker.getValue() == null ? editingExpense.getDate() : datePicker.getValue().toString()
        );

        expenseDAO.updateExpense(editingExpense);

        loadExpenses();
        clearForm();

        showAlert(Alert.AlertType.INFORMATION, "Expense Updated", "Your expense has been updated.");
    }


    @FXML
    private void handleDeleteExpense() {

        Expense selected = expenseTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an expense first.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Expense");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this expense?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            expenseDAO.deleteExpense(selected.getId());
            loadExpenses();
            clearForm();
        }
    }


    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        categoryComboBox.setValue(null);
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(null);
        editingExpense = null;
        expenseTable.getSelectionModel().clearSelection();
    }

    private void fillForm(Expense expense) {

        editingExpense = expense;

        categoryComboBox.setValue(expense.getCategory());
        descriptionField.setText(expense.getDescription());
        amountField.setText(String.valueOf(expense.getAmount()));

        try {
            datePicker.setValue(LocalDate.parse(expense.getDate()));
        } catch (Exception ignored) {
            datePicker.setValue(null);
        }
    }


    private boolean validateFields() {

        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Category", "Please select an expense category.");
            return false;
        }

        String amountText = amountField.getText().trim();

        if (amountText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Amount", "Please enter the expense amount.");
            return false;
        }

        try {

            double amount = Double.parseDouble(amountText);

            if (amount < 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount cannot be negative.");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid numeric amount.");
            return false;
        }

        return true;
    }


    @FXML
    private void handleBack() {

        Stage stage = (Stage) expenseTable.getScene().getWindow();

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
