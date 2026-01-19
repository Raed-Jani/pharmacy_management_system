package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private Button btnProduits;

    @FXML
    private Button btnVentes;

    @FXML
    private Button btnClients;

    @FXML
    private Button btnCommandes;

    @FXML
    private Button btnRapports;

    @FXML
    private Button btnUtilisateurs;

    @FXML
    private Button btnLogs;

    @FXML
    private Label lblAlertes;

    @FXML
    private Label lblStatsVentes;

    @FXML
    private Label lblStatsProduits;

    private Utilisateur utilisateurConnecte;
    private GestionStock gestionStock;
    private GestionVente gestionVente;

    /**
     * Initialisation du contrôleur.
     */
    @FXML
    public void initialize() {
        System.out.println("✓ DashboardController initialisé");

        try {
            gestionStock = new GestionStock();
            gestionVente = new GestionVente();

            // Charger la page d'accueil par défaut
            chargerAccueil();

            System.out.println("✓ Dashboard prêt");

        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Définit l'utilisateur connecté et configure l'interface.
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;

        System.out.println("✓ Utilisateur défini: " + utilisateur.getLogin());

        // Afficher les informations de l'utilisateur
        lblBienvenue.setText("Bienvenue, " + utilisateur.getLogin());
        lblRole.setText("Rôle: " + utilisateur.getRole());

        // Masquer les boutons admin si l'utilisateur n'est pas admin
        if (!utilisateur.estAdmin()) {
            if (btnUtilisateurs != null) {
                btnUtilisateurs.setVisible(false);
                btnUtilisateurs.setManaged(false);
            }
            if (btnLogs != null) {
                btnLogs.setVisible(false);
                btnLogs.setManaged(false);
            }
            if (btnRapports != null) {
                btnRapports.setVisible(false);
                btnRapports.setManaged(false);
            }
        }

        // Charger les statistiques
        chargerStatistiques();
    }

    /**
     * Charge la page d'accueil.
     */
    private void chargerAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Accueil.fxml"));
            AnchorPane accueil = loader.load();
            contentArea.setCenter(accueil);
            System.out.println("✓ Page d'accueil chargée");
        } catch (Exception e) {
            System.err.println("⚠ Impossible de charger Accueil.fxml (optionnel)");
            // Afficher un message simple à la place
            Label bienvenue = new Label("Bienvenue dans le système de gestion de pharmacie\n\nUtilisez le menu de gauche pour naviguer");
            bienvenue.setStyle("-fx-font-size: 18px; -fx-text-alignment: center;");
            VBox container = new VBox(bienvenue);
            container.setStyle("-fx-alignment: center; -fx-padding: 50;");
            contentArea.setCenter(container);
        }
    }

    /**
     * Charge les statistiques du tableau de bord.
     */
    private void chargerStatistiques() {
        new Thread(() -> {
            try {
                // Compter les produits en alerte
                int nbAlertes = gestionStock.getProduitsEnAlerte().size();

                // Compter le nombre de ventes
                int nbVentes = gestionVente.listerVentes().size();

                // Compter le nombre total de produits
                int nbProduits = gestionStock.listerTousProduits().size();

                // Calculer le chiffre d'affaires
                double ca = gestionVente.calculerChiffreAffaires();

                Platform.runLater(() -> {
                    lblAlertes.setText(nbAlertes + " produit(s) en alerte");
                    lblStatsVentes.setText(String.format("CA: %.2f TND (%d ventes)", ca, nbVentes));
                    lblStatsProduits.setText(nbProduits + " produits en stock");

                    System.out.println("✓ Statistiques chargées:");
                    System.out.println("  - Produits en alerte: " + nbAlertes);
                    System.out.println("  - Total produits: " + nbProduits);
                    System.out.println("  - Ventes: " + nbVentes);
                    System.out.println("  - CA: " + String.format("%.2f TND", ca));
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblAlertes.setText("Erreur de chargement");
                });
                System.err.println("Erreur chargement stats: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Gère le clic sur le bouton Produits.
     */
    @FXML
    private void handleProduits() {
        System.out.println("→ Navigation: Gestion des Produits");
        chargerVue("/fxml/GestionProduits.fxml", "Gestion des Produits");
    }

    /**
     * Gère le clic sur le bouton Ventes.
     */
    @FXML
    private void handleVentes() {
        System.out.println("→ Navigation: Gestion des Ventes");
        chargerVue("/fxml/GestionVentes.fxml", "Gestion des Ventes");
    }

    /**
     * Gère le clic sur le bouton Clients.
     */
    @FXML
    private void handleClients() {
        System.out.println("→ Navigation: Gestion des Clients");
        chargerVue("/fxml/GestionClients.fxml", "Gestion des Clients");
    }

    /**
     * Gère le clic sur le bouton Commandes.
     */
    @FXML
    private void handleCommandes() {
        System.out.println("→ Navigation: Gestion des Commandes");
        chargerVue("/fxml/GestionCommandes.fxml", "Gestion des Commandes");
    }

    /**
     * Gère le clic sur le bouton Rapports.
     */
    @FXML
    private void handleRapports() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            System.out.println("→ Navigation: Rapports");
            chargerVue("/fxml/Rapports.fxml", "Rapports et Statistiques");
        }
    }

    /**
     * Gère le clic sur le bouton Utilisateurs.
     */
    @FXML
    private void handleUtilisateurs() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            System.out.println("→ Navigation: Gestion des Utilisateurs");
            chargerVue("/fxml/GestionUtilisateurs.fxml", "Gestion des Utilisateurs");
        }
    }

    /**
     * Gère le clic sur le bouton Logs.
     */
    @FXML
    private void handleLogs() {
        if (utilisateurConnecte != null && utilisateurConnecte.estAdmin()) {
            System.out.println("→ Navigation: Consultation des Logs");
            chargerVue("/fxml/ConsultationLogs.fxml", "Consultation des Logs");
        }
    }

    /**
     * Charge une vue dans la zone de contenu.
     */
    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane vue = loader.load();

            // Passer l'utilisateur au contrôleur si nécessaire
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setUtilisateur(utilisateurConnecte);
            }

            contentArea.setCenter(vue);
            System.out.println("✓ Vue chargée: " + titre);

        } catch (Exception e) {
            System.err.println("⚠ Impossible de charger: " + titre);
            e.printStackTrace();

            // Afficher un message temporaire
            Label message = new Label("Module en cours de développement\n\n" + titre);
            message.setStyle("-fx-font-size: 16px; -fx-text-alignment: center;");
            VBox container = new VBox(message);
            container.setStyle("-fx-alignment: center; -fx-padding: 50;");
            contentArea.setCenter(container);
        }
    }

    /**
     * Gère la déconnexion.
     */
    @FXML
    private void handleDeconnexion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("✓ Déconnexion de: " + utilisateurConnecte.getLogin());

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                    Scene scene = new Scene(loader.load());

                    try {
                        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    } catch (Exception e) {
                        // CSS optionnel
                    }

                    Stage stage = (Stage) lblBienvenue.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.centerOnScreen();
                    stage.show();

                    System.out.println("✓ Retour à l'écran de connexion");

                } catch (Exception e) {
                    e.printStackTrace();
                    afficherErreur("Erreur", "Impossible de retourner à l'écran de connexion");
                }
            }
        });
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Rafraîchit les statistiques.
     */
    @FXML
    private void handleRafraichir() {
        System.out.println("↻ Rafraîchissement des statistiques...");
        chargerStatistiques();
    }
}

/**
 * Interface de base pour tous les contrôleurs.
 */
interface BaseController {
    void setUtilisateur(Utilisateur utilisateur);
}
