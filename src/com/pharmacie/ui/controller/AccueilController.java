package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.application.Platform;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.DashboardStatisticsService;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

/**
 * Contrôleur pour la page d'accueil du tableau de bord.
 * Implémente un design premium avec alertes dynamiques et actions rapides.
 */
public class AccueilController extends BaseController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label lblTotalProducts;
    @FXML
    private Label lblLowStockItems;
    @FXML
    private Label lblTodaysSales;
    @FXML
    private Label lblPendingOrders;
    @FXML
    private VBox vboxStockAlerts;
    @FXML
    private VBox vboxActivites;

    @FXML
    private Button btnQuickRapports;
    @FXML
    private Button btnQuickUtilisateurs;

    private DashboardStatisticsService statsService;
    private boolean serviceDisponible = false;

    @FXML
    public void initialize() {
        try {
            statsService = new DashboardStatisticsService();
            serviceDisponible = true;
            refreshData();
        } catch (ConnexionEchoueeException e) {
            System.err.println("⚠ Erreur de connexion stats: " + e.getMessage());
            serviceDisponible = false;
        }
    }

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (utilisateurConnecte != null) {
            boolean isAdmin = utilisateurConnecte.estAdmin();
            if (btnQuickRapports != null) {
                btnQuickRapports.setVisible(isAdmin);
                btnQuickRapports.setManaged(isAdmin);
            }
            if (btnQuickUtilisateurs != null) {
                btnQuickUtilisateurs.setVisible(isAdmin);
                btnQuickUtilisateurs.setManaged(isAdmin);
            }
        }
    }

    public void refreshData() {
        if (!serviceDisponible || statsService == null)
            return;

        new Thread(() -> {
            try {
                // 1. Fetch Data
                Map<String, Object> stocks = statsService.getEtatStocks();
                BigDecimal caJour = statsService.getChiffreAffairesPeriode(LocalDate.now(), LocalDate.now());
                List<Map<String, Object>> alertes = statsService.getProduitsEnAlerte();
                List<Map<String, Object>> activities = statsService.getVentesRecentes(8);

                // 2. Update UI
                Platform.runLater(() -> {
                    updateKpis(stocks, caJour);
                    updateAlerts(alertes);
                    updateActivities(activities);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateKpis(Map<String, Object> stocks, BigDecimal caJour) {
        if (lblTotalProducts != null)
            lblTotalProducts.setText(stocks.getOrDefault("totalProduits", "0").toString());
        if (lblLowStockItems != null)
            lblLowStockItems.setText(stocks.getOrDefault("enAlerte", "0").toString());
        if (lblTodaysSales != null)
            lblTodaysSales.setText(String.format("%.2f TND", caJour));
        if (lblPendingOrders != null)
            lblPendingOrders.setText("--"); // Commande data not yet in service
    }

    private void updateAlerts(List<Map<String, Object>> alertes) {
        if (vboxStockAlerts == null)
            return;
        vboxStockAlerts.getChildren().clear();

        if (alertes.isEmpty()) {
            Label msg = new Label("✅ Tout est en ordre. Aucun produit en alerte.");
            msg.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-padding: 10;");
            vboxStockAlerts.getChildren().add(msg);
            return;
        }

        for (Map<String, Object> alerte : alertes) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 20, 12, 20));

            int stock = (int) alerte.get("stock");
            boolean isCritical = (stock == 0);
            row.getStyleClass().addAll("stock-alert-item", isCritical ? "stock-alert-critical" : "stock-alert-warning");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label nom = new Label(alerte.get("nom").toString());
            nom.getStyleClass().add("stock-alert-product-name");

            Label details = new Label("Stock: " + stock + " | Seuil: " + alerte.get("seuil"));
            details.getStyleClass().add("stock-alert-quantity");

            info.getChildren().addAll(nom, details);

            Label status = new Label(isCritical ? "❌ RUPTURE" : "⚠ BAS STOCK");
            status.setStyle("-fx-font-weight: 900; -fx-text-fill: " + (isCritical ? "#e74c3c" : "#f39c12") + ";");

            row.getChildren().addAll(info, status);
            vboxStockAlerts.getChildren().add(row);
        }
    }

    private void updateActivities(List<Map<String, Object>> activities) {
        if (vboxActivites == null)
            return;
        vboxActivites.getChildren().clear();

        for (Map<String, Object> act : activities) {
            HBox item = new HBox(15);
            item.getStyleClass().add("activity-item");
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 0, 10, 0));

            Label icon = new Label("💰");
            VBox info = new VBox(2);
            Label desc = new Label("Vente #" + act.get("id") + " - " + act.get("client"));
            desc.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            Label time = new Label("Montant: " + act.get("total") + " TND");
            time.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            info.getChildren().addAll(desc, time);
            item.getChildren().addAll(icon, info);
            vboxActivites.getChildren().add(item);
        }
    }

    // Navigation
    @FXML
    private void handleNewSale() {
        if (dashboardController != null)
            dashboardController.handleVentes();
    }

    @FXML
    private void handleNewOrder() {
        if (dashboardController != null)
            dashboardController.handleCommandes();
    }

    @FXML
    private void handleGoToRapports() {
        if (dashboardController != null)
            dashboardController.handleRapports();
    }

    @FXML
    private void handleGoToUtilisateurs() {
        if (dashboardController != null)
            dashboardController.handleUtilisateurs();
    }
}