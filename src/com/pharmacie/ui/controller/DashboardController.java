package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.pharmacie.model.Utilisateur;
import java.io.IOException;

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
    public void initialize() {
        // Initial view load is triggered by setUtilisateur
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof BaseController) {
                BaseController base = (BaseController) ctrl;
                base.setUtilisateur(this.user);
                base.setDashboard(this);
            }
            contentArea.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Load Error", "Failed to load " + title + ": " + e.getMessage());
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
