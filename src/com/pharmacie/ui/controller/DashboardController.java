package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.GestionStock;
import com.pharmacie.service.GestionVente;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.SQLException;

/**
 * Contrôleur principal du tableau de bord.
 */
public class DashboardController {

    @FXML
    private Label lblBienvenue;
    @FXML
    private Label lblRole;
    @FXML
    private BorderPane contentArea;
    @FXML
    private VBox vboxAdmin;

    private Utilisateur utilisateurConnecte;
    private GestionStock gestionStock;
    private GestionVente gestionVente;

    @FXML
    public void initialize() {
        System.out.println("✓ DashboardController initialisé");
        try {
            gestionStock = new GestionStock();
            gestionVente = new GestionVente();
            chargerAccueil();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        if (lblBienvenue != null)
            lblBienvenue.setText("Bienvenue, " + utilisateur.getLogin());
        if (lblRole != null)
            lblRole.setText("Rôle: " + utilisateur.getRole());
        if (!utilisateur.estAdmin() && vboxAdmin != null) {
            vboxAdmin.setVisible(false);
            vboxAdmin.setManaged(false);
        }
    }

    @FXML
    public void handleRafraichir() {
        chargerAccueil();
    }

    public void chargerAccueil() {
        chargerVue("/fxml/Accueil.fxml", "Tableau de Bord");
    }

    @FXML
    public void handleProduits() {
        chargerVue("/fxml/GestionProduits.fxml", "Gestion des Produits");
    }

    @FXML
    public void handleVentes() {
        chargerVue("/fxml/GestionVentes.fxml", "Gestion des Ventes");
    }

    @FXML
    public void handleClients() {
        chargerVue("/fxml/GestionClients.fxml", "Gestion des Clients");
    }

    @FXML
    public void handleCommandes() {
        chargerVue("/fxml/GestionCommandes.fxml", "Gestion des Commandes");
    }

    @FXML
    public void handleFournisseurs() {
        chargerVue("/fxml/GestionFournisseurs.fxml", "Gestion des Fournisseurs");
    }

    @FXML
    public void handleRapports() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            chargerVue("/fxml/Rapports.fxml", "Rapports");
        }
    }

    @FXML
    public void handleUtilisateurs() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            chargerVue("/fxml/GestionUtilisateurs.fxml", "Gestion des Utilisateurs");
        }
    }

    @FXML
    public void handleLogs() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            chargerVue("/fxml/ConsultationLogs.fxml", "Logs");
        }
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vue = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                BaseController base = (BaseController) controller;
                base.setUtilisateur(utilisateurConnecte);
                base.setDashboardController(this);
            }
            contentArea.setCenter(vue);
        } catch (Exception e) {
            e.printStackTrace();
            Label message = new Label("Module: " + titre);
            contentArea.setCenter(new VBox(message));
        }
    }

    @FXML
    private void handleDeconnexion() {
        // Logic for logout shortened for brevity
        Platform.exit();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}