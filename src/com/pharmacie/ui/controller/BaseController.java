package com.pharmacie.ui.controller;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.ui.notification.ToastNotification;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.Optional;

/**
 * Base class for all UI controllers, providing common utilities for
 * user management, navigation feedback, and notifications.
 */
public abstract class BaseController {

    protected Utilisateur user;
    protected DashboardController dashboard;

    @FXML
    protected Label lblHeaderName; // Common header title if present

    public void setUtilisateur(Utilisateur user) {
        this.user = user;
    }

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    /**
     * Get the root Pane for displaying toast notifications.
     * Delegates to the dashboard controller if available.
     */
    protected Pane getRootPane() {
        if (dashboard != null) {
            return dashboard.getRootPane();
        }

        // Fallback: search for the scene root if not in a dashboard context
        if (lblHeaderName != null && lblHeaderName.getScene() != null) {
            Node root = lblHeaderName.getScene().getRoot();
            if (root instanceof Pane) {
                return (Pane) root;
            }
        }
        return null;
    }

    protected void showInfo(String title, String message) {
        showToast(message, ToastNotification.Type.INFO);
    }

    protected void showSuccess(String message) {
        showToast(message, ToastNotification.Type.SUCCESS);
    }

    protected void showError(String title, String message) {
        showToast(message, ToastNotification.Type.ERROR);
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

    /**
     * Display a toast notification (non-blocking)
     */
    private void showToast(String message, ToastNotification.Type type) {
        Pane rootPane = getRootPane();
        if (rootPane != null) {
            ToastNotification.show(rootPane, message, type);
        } else {
            System.out.println("TOAST FALLBACK [" + type + "] " + message);
        }
    }

    /**
     * Show a confirmation dialog (modal - requires user interaction)
     */
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
}
