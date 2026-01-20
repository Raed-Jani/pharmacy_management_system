package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.model.LogActivite;
import com.pharmacie.service.AuthenticationService;
import com.pharmacie.exception.ConnexionEchoueeException;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Contrôleur pour l'écran de connexion.
 */
public class LoginController {

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

    private AuthenticationService authService;
    private LogActiviteDAO logDAO;

    /**
     * Ouvre le tableau de bord principal.
     */
    private void ouvrirDashboard(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // Passer l'utilisateur au contrôleur du dashboard
            DashboardController controller = loader.getController();
            controller.setUtilisateur(utilisateur);

            // Appliquer le CSS
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("⚠ CSS non chargé");
            }

            // Obtenir le stage actuel et changer la scène
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();

            System.out.println("✓ Dashboard affiché");
            System.out.println("================================================\n");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'ouverture du dashboard:");
            e.printStackTrace();
            afficherErreur("Erreur lors de l'ouverture du tableau de bord: " + e.getMessage());
        }
    }

    /**
     * Initialisation du contrôleur.
     */
    @FXML
    public void initialize() {

        try {
            authService = AuthenticationService.getInstance();
            logDAO = new LogActiviteDAO();

            // Cacher le message d'erreur et l'indicateur de progression
            if (lblError != null) {
                lblError.setVisible(false);
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }

            // Permettre la connexion avec la touche Entrée
            if (txtPassword != null) {
                txtPassword.setOnKeyPressed(this::handleKeyPressed);
            }

        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur d'initialisation du contrôleur:");
            e.printStackTrace();
            afficherErreur("Erreur de connexion à la base de données");
        }
    }

    /**
     * Gère la pression de la touche Entrée.
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Gère le clic sur le bouton de connexion.
     */
    @FXML
    private void handleLogin() {

        String login = txtLogin.getText().trim();
        String password = txtPassword.getText();

        // Validation des champs
        if (login.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs");
            return;
        }

        // Afficher l'indicateur de progression
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        if (btnLogin != null) {
            btnLogin.setDisable(true);
        }
        if (lblError != null) {
            lblError.setVisible(false);
        }

        // Authentification dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                Utilisateur utilisateur = authService.login(login, password);

                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (btnLogin != null) {
                        btnLogin.setDisable(false);
                    }

                    if (utilisateur != null) {

                        // Log (déjà géré dans AuthenticationService, mais on peut garder l'affichage
                        // console)

                        // Afficher un message de succès temporaire
                        afficherSucces("Connexion réussie ! Bienvenue " + utilisateur.getLogin());

                        // Ouvrir le Dashboard
                        ouvrirDashboard(utilisateur);

                    } else {
                        System.out.println("✗ Authentification échouée");
                        afficherErreur("Login ou mot de passe incorrect");
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (btnLogin != null) {
                        btnLogin.setDisable(false);
                    }

                    System.err.println("✗ Erreur SQL: " + e.getMessage());
                    e.printStackTrace();
                    afficherErreur("Erreur lors de l'authentification: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Affiche un message d'erreur.
     */
    private void afficherErreur(String message) {
        if (lblError != null) {
            lblError.setText("❌ " + message);
            lblError.setVisible(true);
            lblError.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");

            // Animation de vibration
            try {
                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                        javafx.util.Duration.millis(50), lblError);
                tt.setFromX(0);
                tt.setByX(10);
                tt.setCycleCount(4);
                tt.setAutoReverse(true);
                tt.play();
            } catch (Exception e) {
                // Animation optionnelle
            }
        }
        System.err.println("❌ " + message);
    }

    /**
     * Affiche un message de succès.
     */
    private void afficherSucces(String message) {
        if (lblError != null) {
            lblError.setText("✓ " + message);
            lblError.setVisible(true);
            lblError.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        }
        System.out.println("✓ " + message);
    }

    /**
     * Ferme l'application.
     */
    @FXML
    private void handleQuitter() {
        System.out.println("\n✓ Fermeture de l'application");
        Platform.exit();
    }
}