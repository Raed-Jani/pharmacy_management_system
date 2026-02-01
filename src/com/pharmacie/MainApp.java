package com.pharmacie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;
import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BioVera Pro v1.1 (New Build) - Système de Gestion de Pharmacie");

        loadAppIcon();

        if (!testerConnexionBD()) {
            afficherErreurConnexion();
            return;
        }

        afficherLogin();
    }

    private void loadAppIcon() {
        try {
            this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        } catch (Exception e) {
            System.err.println("Note: Impossible de charger l'icône de l'application: " + e.getMessage());
        }
    }

    private boolean testerConnexionBD() {
        try {
            DBConnection db = DBConnection.getInstance();
            db.getAdminConnection();
            return true;
        } catch (ConnexionEchoueeException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            System.err.println("Aide: " + e.getMessageAide());
            return false;
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void afficherErreurConnexion() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de connexion");
        alert.setHeaderText("Impossible de se connecter à la base de données");
        alert.setContentText(
                "Vérifiez que:\n" +
                        "1. MySQL est démarré\n" +
                        "2. La base 'pharmacie_db' existe\n" +
                        "3. Le mot de passe root est correct dans DBConnection.java\n\n" +
                        "L'application va se fermer.");
        alert.showAndWait();
        System.exit(1);
    }

    public void afficherLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            loadCSS(scene);

            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Login.fxml: " + e.getMessage());
            e.printStackTrace();
            afficherErreurFXML();
        }
    }

    private void loadCSS(Scene scene) {
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            // Add theme toggle button CSS for visibility
            String themeButtonCss = getClass().getResource("/css/theme-toggle-button.css").toExternalForm();
            scene.getStylesheets().add(themeButtonCss);
        } catch (Exception e) {
            // CSS optionnel, on continue sans si non trouvé
        }
    }

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
                        "   → Mark Directory as → Resources Root");
        alert.showAndWait();
        System.exit(1);
    }

    public void afficherDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            loadCSS(scene);

            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du tableau de bord");
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        try {
            DBConnection.getInstance().closeAllConnections();
        } catch (ConnexionEchoueeException e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
