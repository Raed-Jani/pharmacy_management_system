package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.AuthenticationService;
import java.io.IOException;

public class LoginController extends BaseController {

    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Label lblError;
    @FXML
    private ProgressIndicator progressIndicator;

    private final AuthenticationService authService = AuthenticationService.getInstance();

    @FXML
    public void initialize() {
        if (lblError != null)
            lblError.setVisible(false);
        if (progressIndicator != null)
            progressIndicator.setVisible(false);
        if (txtPassword != null)
            txtPassword.setOnKeyPressed(this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER)
            handleLogin();
    }

    @FXML
    private void handleLogin() {
        String login = txtLogin.getText().trim();
        String password = txtPassword.getText();

        if (login.isEmpty() || password.isEmpty()) {
            displayError("Please fill in all fields");
            return;
        }

        setLoading(true);
        new Thread(() -> {
            try {
                Utilisateur authenticatedUser = authService.login(login, password);
                Platform.runLater(() -> {
                    setLoading(false);
                    if (authenticatedUser != null) {
                        this.user = authenticatedUser;
                        openDashboard();
                    } else {
                        displayError("Invalid credentials");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    displayError("Login error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null)
            progressIndicator.setVisible(loading);
        if (btnLogin != null)
            btnLogin.setDisable(loading);
        if (lblError != null && loading)
            lblError.setVisible(false);
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUtilisateur(this.user);

            Scene scene = btnLogin.getScene();
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            if (!stage.isMaximized())
                stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Dashboard Load Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void displayError(String message) {
        if (lblError != null) {
            lblError.setText("❌ " + message);
            lblError.setVisible(true);
            lblError.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleQuitter() {
        Platform.exit();
    }
}
