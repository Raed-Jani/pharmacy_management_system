package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.ThemeManager;
import java.io.IOException;
import java.net.URL;

public class DashboardController extends BaseController {

    @FXML
    private Label lblBienvenue;
    @FXML
    private Label lblRole;
    @FXML
    private BorderPane contentArea;
    @FXML
    private VBox vboxAdmin;
    @FXML
    private StackPane dashboardRoot; // Bound to the root StackPane
    @FXML
    private StackPane toastOverlay; // Bound via FXML (dedicated overlay)
    @FXML
    private Button btnThemeToggle; // Theme toggle button

    /**
     * Get the StackPane container for notifications.
     * Includes a fail-safe programmatic creation if injection fails.
     */
    public Pane getRootPane() {
        // 1. Check injected field
        if (toastOverlay != null)
            return toastOverlay;

        // 2. Search in scene if fields are null
        if (dashboardRoot != null && dashboardRoot.getScene() != null) {
            Node found = dashboardRoot.getScene().lookup("#toastOverlay");
            if (found instanceof Pane) {
                toastOverlay = (StackPane) found;
                return toastOverlay;
            }
        }

        // 3. Last resort fallback
        return (dashboardRoot != null) ? dashboardRoot : contentArea;
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG DashboardController: initialize() called");

        // Initialize theme manager
        ThemeManager.initialize();

        // Use Platform.runLater to wait for full scene attachment
        Platform.runLater(() -> {
            if (toastOverlay == null && dashboardRoot != null && dashboardRoot.getScene() != null) {
                Node found = dashboardRoot.getScene().lookup("#toastOverlay");
                if (found instanceof StackPane) {
                    toastOverlay = (StackPane) found;
                    System.out.println("DEBUG DashboardController: toastOverlay recovered via manual lookup");
                }
            }

            if (toastOverlay != null) {
                toastOverlay.setPickOnBounds(false);
                toastOverlay.setMouseTransparent(false);
            }

            // DEBUG : VERIFY BUTTON INJECTION
            if (btnThemeToggle == null) {
                System.err.println("CRITICAL ERROR: btnThemeToggle IS NULL in Controller!");
            } else {
                System.out.println("DEBUG: btnThemeToggle is injected correctly.");
                btnThemeToggle.setVisible(true);
                btnThemeToggle.setStyle("-fx-background-color: purple; -fx-text-fill: lime; -fx-font-size: 20px;");
                btnThemeToggle.toFront();
            }

            // Apply saved theme and update button icon
            if (dashboardRoot != null) {
                ThemeManager.applyTheme(dashboardRoot);
                if (btnThemeToggle != null) {
                    btnThemeToggle.setText(ThemeManager.getThemeIcon());
                }
            }
        });
    }

    @Override
    public void setUtilisateur(Utilisateur user) {
        super.setUtilisateur(user);
        if (lblBienvenue != null)
            lblBienvenue.setText("Welcome, " + user.getLogin());
        if (lblRole != null)
            lblRole.setText("Role: " + user.getRole());
        if (!user.estAdmin() && vboxAdmin != null) {
            vboxAdmin.setVisible(false);
            vboxAdmin.setManaged(false);
        }
        loadAccueil();
    }

    @FXML
    public void handleRafraichir() {
        loadAccueil();
    }

    @FXML
    public void handleToggleTheme() {
        if (dashboardRoot != null) {
            String newIcon = ThemeManager.toggleTheme(dashboardRoot);
            if (btnThemeToggle != null) {
                btnThemeToggle.setText(newIcon);
            }
        }
    }

    public void loadAccueil() {
        loadView("/fxml/Accueil.fxml", "Dashboard Home");
    }

    @FXML
    public void handleProduits() {
        loadView("/fxml/GestionProduits.fxml", "Product Management");
    }

    @FXML
    public void handleVentes() {
        loadView("/fxml/GestionVentes.fxml", "Sales Management");
    }

    @FXML
    public void handleClients() {
        loadView("/fxml/GestionClients.fxml", "Client Management");
    }

    @FXML
    public void handleCommandes() {
        loadView("/fxml/GestionCommandes.fxml", "Order Management");
    }

    @FXML
    public void handleFournisseurs() {
        loadView("/fxml/GestionFournisseurs.fxml", "Supplier Management");
    }

    @FXML
    public void handleRapports() {
        if (user != null && user.estAdmin())
            loadView("/fxml/Rapports.fxml", "Reports");
    }

    @FXML
    public void handleUtilisateurs() {
        if (user != null && user.estAdmin())
            loadView("/fxml/GestionUtilisateurs.fxml", "User Management");
    }

    @FXML
    public void handleLogs() {
        if (user != null && user.estAdmin())
            loadView("/fxml/ConsultationLogs.fxml", "Activity Logs");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML resource not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof BaseController) {
                BaseController base = (BaseController) ctrl;
                base.setUtilisateur(this.user);
                base.setDashboard(this);
            }
            contentArea.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Load Error",
                    "Failed to load " + title + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @FXML
    public void handleDeconnexion() {
        if (confirm("Logout", "Are you sure you want to log out?")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                Parent root = loader.load();
                Scene scene = contentArea.getScene();
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (IOException e) {
                showError("Error", "Failed to return to login screen.");
            }
        }
    }

    @FXML
    public void handleLogout() {
        handleDeconnexion();
    }
}
