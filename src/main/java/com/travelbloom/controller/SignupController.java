package com.travelbloom.controller;

import com.travelbloom.dao.UserDAO;
import com.travelbloom.model.User;
import com.travelbloom.service.EmailService;
import com.travelbloom.util.Session;
import com.travelbloom.util.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();


    @FXML
    private void initialize() {
        errorLabel.setText("");
    }


    @FXML
    private void handleSignup() {

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        if (username.length() < 3) {
            errorLabel.setText("Username must be at least 3 characters.");
            return;
        }

        if (password.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // Email is optional, but if the user typed one, it must be valid —
        // otherwise the welcome email would silently never arrive.
        if (!email.isEmpty() && !emailService.isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address, or leave it blank.");
            return;
        }

        String error = userDAO.register(username, email, password);

        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        // Fire-and-forget — never blocks signup, and does nothing quietly
        // if no email was given or the Resend key isn't set yet.
        if (!email.isEmpty()) {
            emailService.sendWelcomeEmailAsync(email, username);
        }

        // Auto-login right after signing up
        User newUser = userDAO.authenticate(username, password);
        Session.login(newUser);

        navigateToDashboard();
    }


    @FXML
    private void handleGoToLogin() {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/login.fxml", "TravelBloom - Login");
    }


    private void navigateToDashboard() {
        Stage stage = (Stage) signupButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/dashboard.fxml", "TravelBloom - Smart Trip Planner");
    }
}
