package com.travelbloom.controller;

import com.travelbloom.dao.UserDAO;
import com.travelbloom.model.User;
import com.travelbloom.util.Session;
import com.travelbloom.util.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signupLink;

    private final UserDAO userDAO = new UserDAO();


    @FXML
    private void initialize() {
        errorLabel.setText("");
    }


    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            return;
        }

        Session.login(user);

        navigateToDashboard();
    }


    @FXML
    private void handleGoToSignup() {
        Stage stage = (Stage) signupLink.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/signup.fxml", "TravelBloom - Sign Up");
    }


    private void navigateToDashboard() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/view/dashboard.fxml", "TravelBloom - Smart Trip Planner");
    }
}
