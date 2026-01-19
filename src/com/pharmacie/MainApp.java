package com.pharmacie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.io.IOException;

/**
 * Classe principale de l'application JavaFX.
 * Point d'entrée du système de gestion de pharmacie.
 */
public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Système de Gestion de Pharmacie");

        System.out.println("\n========== DÉMARRAGE DE L'APPLICATION ==========");

        // Tester la connexion à la base de données
        if (!testerConnexionBD()) {
            afficherErreurConnexion();
            return;
        }

        System.out.println("✓ Initialisation réussie");
        System.out.println("✓ Chargement de l'interface de connexion...\n");

        // Afficher l'écran de connexion
        afficherLogin();
    }

    /**
     * Teste la connexion à la base de données.
     */
    private boolean testerConnexionBD() {
        try {
            DBConnection db = DBConnection.getInstance();
            db.getAdminConnection();
            System.out.println("✓ Connexion à la base de données établie");
            return true;
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur de connexion: " + e.getMessage());
            System.err.println("Aide: " + e.getMessageAide());
            return false;
        } catch (Exception e) {
            System.err.println("✗ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Affiche un message d'erreur si la connexion échoue.
     */
    private void afficherErreurConnexion() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de connexion");
        alert.setHeaderText("Impossible de se connecter à la base de données");
        alert.setContentText(
                "Vérifiez que:\n" +
                        "1. MySQL est démarré\n" +
                        "2. La base 'pharmacie_db' existe\n" +
                        "3. Le mot de passe root est correct dans DBConnection.java\n\n" +
                        "L'application va se fermer."
        );
        alert.showAndWait();
        System.exit(1);
    }

    /**
     * Affiche l'écran de connexion.
     */
    public void afficherLogin() {
        System.out.println("Chargement de Login.fxml...");

        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader();

        // Essayer différents chemins possibles
        String[] cheminsPossibles = {
                "/fxml/Login.fxml",
                "resources/fxml/Login.fxml",
                "/Login.fxml"
        };

        Parent root = null;
        String cheminUtilise = null;

        for (String chemin : cheminsPossibles) {
            try {
                System.out.println("  Tentative: " + chemin);
                root = loader.load(getClass().getResourceAsStream(chemin));
                cheminUtilise = chemin;
                System.out.println("  ✓ Trouvé !");
                break;
            } catch (Exception e) {
                System.out.println("  ✗ Non trouvé");
            }
        }

        if (root == null) {
            System.err.println("\n✗ ERREUR: Fichier Login.fxml introuvable !");
            afficherErreurFXML();
            return;
        }

        System.out.println("✓Login.fxml chargé depuis: " + cheminUtilise);

        // Créer la scène
        Scene scene = new Scene(root);

        // Charger le CSS si disponible
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            System.out.println("✓ CSS chargé");
        } catch (Exception e) {
            System.out.println("⚠ CSS non trouvé (l'application fonctionnera sans styles)");
        }

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();

        System.out.println("✓ Interface de connexion affichée\n");
        System.out.println("================================================");
    }

    /**
     * Affiche une erreur si le fichier FXML est introuvable.
     */
    private void afficherErreurFXML() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de chargement");
        alert.setHeaderText("Fichier Login.fxml introuvable");
        alert.setContentText(
                "Structure requise:\n\n" +
                        "pharmacie/\n" +
                        "├── src/\n" +
                        "│   └── com/pharmacie/\n" +
                        "└── resources/\n" +
                        "    └── fxml/\n" +
                        "        └── Login.fxml\n\n" +
                        "SOLUTION:\n" +
                        "1. Créez le dossier 'resources/fxml/'\n" +
                        "2. Placez Login.fxml dedans\n" +
                        "3. Dans IntelliJ: Clic droit sur 'resources'\n" +
                        "   → Mark Directory as → Resources Root"
        );
        alert.showAndWait();
        System.exit(1);
    }

    /**
     * Affiche le tableau de bord principal.
     */
    public void afficherDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("⚠ CSS non chargé");
            }

            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du tableau de bord");
        }
    }

    /**
     * Retourne le stage principal.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        // Fermer les connexions à la base de données
        try {
            DBConnection.getInstance().closeAllConnections();
            System.out.println("\n✓ Connexions fermées");
            System.out.println("✓ Application terminée\n");
        } catch (ConnexionEchoueeException e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   SYSTÈME DE GESTION DE PHARMACIE - VERSION 1.0      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        launch(args);
    }
}