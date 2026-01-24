package com.pharmacie.ui.controller;

import com.pharmacie.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import java.util.Optional;

public abstract class BaseController {

    protected Utilisateur user;
    protected DashboardController dashboard;

    @FXML
    protected Label lblHeaderName;
    @FXML
    protected Label lblHeaderRole;

    public void setUtilisateur(Utilisateur user) {
        this.user = user;
        updateHeaderUserInfo();
    }

    protected void updateHeaderUserInfo() {
        if (user != null) {
            if (lblHeaderName != null)
                lblHeaderName.setText(user.getLogin());
            if (lblHeaderRole != null)
                lblHeaderRole.setText(user.getRole());
        }
    }

    @FXML
    protected void handleLogout() {
        if (dashboard != null) {
            dashboard.handleDeconnexion();
        }
    }

    public Utilisateur getUtilisateur() {
        return user;
    }

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    protected void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    protected void showSuccess(String message) {
        showInfo("Success", message);
    }

    protected void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    protected void afficherErreur(String title, String message) {
        showError(title, message);
    }

    protected void afficherSucces(String title, String message) {
        showSuccess(message);
    }

    protected void afficherInfo(String title, String message) {
        showInfo(title, message);
    }

    protected boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    protected boolean confirmDelete(String target) {
        return confirm("Confirm Deletion",
                "Are you sure you want to delete " + target + "? This action is irreversible.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
