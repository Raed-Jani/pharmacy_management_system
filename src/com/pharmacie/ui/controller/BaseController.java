package com.pharmacie.ui.controller;

import com.pharmacie.model.Utilisateur;
import javafx.scene.control.Alert;

/**
 * Classe abstraite de base pour tous les contrôleurs.
 * Gère l'utilisateur connecté, le contrôleur dashboard et fournit des méthodes
 * utilitaires.
 */
public abstract class BaseController {

    protected Utilisateur utilisateurConnecte;
    protected DashboardController dashboardController;

    /**
     * Définit l'utilisateur connecté pour cette vue.
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    /**
     * Récupère l'utilisateur connecté.
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Définit le contrôleur du tableau de bord.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     */
    protected void afficherErreur(String titre, String message) {
        afficherAlerte(Alert.AlertType.ERROR, titre, message);
    }

    /**
     * Affiche une boîte de dialogue d'information (Succès).
     */
    protected void afficherSucces(String message) {
        afficherAlerte(Alert.AlertType.INFORMATION, "Succès", message);
    }

    /**
     * Affiche un message de succès avec titre.
     */
    protected void afficherSucces(String titre, String message) {
        afficherAlerte(Alert.AlertType.INFORMATION, titre, message);
    }

    /**
     * Affiche une boîte de dialogue d'information standard.
     */
    protected void afficherInformation(String titre, String message) {
        afficherAlerte(Alert.AlertType.INFORMATION, titre, message);
    }

    /**
     * Affiche une boîte de dialogue d'alerte personnalisée.
     */
    protected void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}