package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.DashboardStatisticsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

public class AccueilController extends BaseController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblDashboardSubtitle;
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
    private Button btnQuickSale;
    @FXML
    private Button btnQuickOrder;
    @FXML
    private Button btnQuickProduits;
    @FXML
    private Button btnQuickClients;
    @FXML
    private Button btnQuickRapports;
    @FXML
    private Button btnQuickUtilisateurs;

    @FXML
    private VBox kpiCA;
    @FXML
    private VBox cardVentesChart;
    @FXML
    private VBox cardActivites;

    @FXML
    private LineChart<String, Number> chartVentes;
    @FXML
    private PieChart chartStock;

    private final DashboardStatisticsService statsService = new DashboardStatisticsService();

    @FXML
    public void initialize() {
        if (chartVentes != null) {
            chartVentes.setAnimated(false);
            CategoryAxis xAxis = (CategoryAxis) chartVentes.getXAxis();
            xAxis.setTickLabelRotation(-45);
        }
        refreshData();
        animateEntrance();
        setupAnimations();
    }

    private void animateEntrance() {
        if (scrollPane != null) {
            scrollPane.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), scrollPane);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(100));
            ft.play();
        }
    }

    private void setupAnimations() {
        addHoverAnimation(btnQuickSale);
        addHoverAnimation(btnQuickOrder);
        addHoverAnimation(btnQuickProduits);
        addHoverAnimation(btnQuickClients);
        addHoverAnimation(btnQuickRapports);
        addHoverAnimation(btnQuickUtilisateurs);
    }

    private void addHoverAnimation(Button btn) {
        if (btn == null)
            return;
        btn.setOnMouseEntered(mouseEvent -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(mouseEvent -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    @Override
    public void setUtilisateur(Utilisateur user) {
        super.setUtilisateur(user);
        updateUIForRole();
    }

    private void updateUIForRole() {
        if (user != null) {
            boolean isAdmin = user.estAdmin();
            if (lblWelcome != null) {
                lblWelcome.setText(isAdmin ? "Admin Dashboard" : "Welcome, " + user.getLogin());
            }
            if (lblDashboardSubtitle != null) {
                lblDashboardSubtitle.setText(isAdmin ? "Global overview of pharmacy activity" : "Ready for a new day?");
            }

            setVisibility(kpiCA, isAdmin);
            setVisibility(cardVentesChart, isAdmin);
            setVisibility(cardActivites, isAdmin);
            setVisibility(btnQuickRapports, isAdmin);
            setVisibility(btnQuickUtilisateurs, isAdmin);
        }
    }

    private void setVisibility(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    @FXML
    private void handleRafraichir() {
        refreshData();
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                Map<String, Object> stocks = statsService.getStockSummary();
                BigDecimal caJour = statsService.getRevenueForPeriod(LocalDate.now(), LocalDate.now());
                List<Map<String, Object>> alerts = statsService.getAlertProducts();
                List<Map<String, Object>> activities = statsService.getRecentSales(8);
                List<String> caData = statsService.getDailyRevenueHistory(7);
                int pending = statsService.getPendingOrdersCount();

                Platform.runLater(() -> {
                    updateKpis(stocks, caJour, pending);
                    updateAlerts(alerts);
                    updateActivities(activities);
                    updateCharts(stocks, caData);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing dashboard data: " + e.getMessage());
            }
        }).start();
    }

    private void updateKpis(Map<String, Object> stocks, BigDecimal caJour, int pending) {
        if (lblTotalProducts != null)
            lblTotalProducts.setText(stocks.getOrDefault("totalProducts", "0").toString());
        if (lblLowStockItems != null)
            lblLowStockItems.setText(stocks.getOrDefault("totalAlerts", "0").toString());
        if (lblTodaysSales != null)
            lblTodaysSales.setText(String.format("%.2f TND", caJour));
        if (lblPendingOrders != null)
            lblPendingOrders.setText(String.valueOf(pending));
    }

    private void updateAlerts(List<Map<String, Object>> alerts) {
        if (vboxStockAlerts == null)
            return;
        vboxStockAlerts.getChildren().clear();

        if (alerts.isEmpty()) {
            Label msg = new Label("✅ Everything in order. No low stock items.");
            msg.getStyleClass().add("alert-empty-message");
            vboxStockAlerts.getChildren().add(msg);
            return;
        }

        for (Map<String, Object> alerte : alerts) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 18, 12, 18));
            row.getStyleClass().add("alert-item");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label nom = new Label(alerte.get("nom").toString());
            nom.getStyleClass().add("alert-item-title");

            Label stock = new Label("Stock: " + alerte.get("stock") + " / Min: " + alerte.get("seuil"));
            stock.getStyleClass().add("alert-item-subtitle");

            info.getChildren().addAll(nom, stock);

            Button btn = new Button("Commander");
            btn.getStyleClass().add("btn-order-small");
            btn.setOnAction(ae -> handleNewOrder());

            row.getChildren().addAll(info, btn);
            vboxStockAlerts.getChildren().add(row);
        }
    }

    private void updateActivities(List<Map<String, Object>> activities) {
        if (vboxActivites == null)
            return;
        vboxActivites.getChildren().clear();

        for (Map<String, Object> act : activities) {
            HBox item = new HBox(15);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 15, 10, 15));
            item.getStyleClass().add("activity-item");

            Label icon = new Label("💰");
            icon.setStyle("-fx-font-size: 18px;");

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label title = new Label("Vente #" + act.get("id") + " - " + act.get("client"));
            title.getStyleClass().add("activity-item-title");

            Label val = new Label(act.get("total") + " TND");
            val.getStyleClass().add("activity-item-value");

            info.getChildren().addAll(title, val);

            item.getChildren().addAll(icon, info);

            vboxActivites.getChildren().add(item);
        }
    }

    private void updateCharts(Map<String, Object> stocks, List<String> caData) {
        if (chartVentes != null) {
            CategoryAxis xAxis = (CategoryAxis) chartVentes.getXAxis();
            xAxis.getCategories().clear(); // CRITICAL: Clear categories to prevent clumping

            chartVentes.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue (7j)");

            if (caData != null) {
                for (String entry : caData) {
                    try {
                        String[] parts = entry.split(":");
                        if (parts.length >= 2) {
                            String date = parts[0].trim();
                            if (date.length() >= 10) {
                                date = date.substring(5); // MM-DD
                            }
                            double amount = Double.parseDouble(parts[1].trim().split(" ")[0].replace(",", "."));
                            series.getData().add(new XYChart.Data<>(date, amount));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing CA chart data: " + e.getMessage());
                    }
                }
            }
            chartVentes.getData().add(series);

            xAxis.setTickLabelRotation(-45);
        }

        if (chartStock != null && stocks != null) {
            chartStock.getData().clear();
            int total = (int) stocks.getOrDefault("totalProducts", 0);
            int low = (int) stocks.getOrDefault("lowStock", 0);
            int out = (int) stocks.getOrDefault("outOfStock", 0);
            if (total > 0) {
                double normalVal = (int) stocks.getOrDefault("totalProducts", 0)
                        - (int) stocks.getOrDefault("lowStock", 0) - (int) stocks.getOrDefault("outOfStock", 0);
                double lowVal = (int) stocks.getOrDefault("lowStock", 0);
                double outVal = (int) stocks.getOrDefault("outOfStock", 0);

                double normalPct = (normalVal / total) * 100;
                double lowPct = (lowVal / total) * 100;
                double outPct = (outVal / total) * 100;

                if (normalVal > 0)
                    chartStock.getData().add(new PieChart.Data(String.format("Normal (%.0f%%)", normalPct), normalVal));
                if (lowVal > 0)
                    chartStock.getData().add(new PieChart.Data(String.format("Bas (%.0f%%)", lowPct), lowVal));
                if (outVal > 0)
                    chartStock.getData().add(new PieChart.Data(String.format("Rupture (%.0f%%)", outPct), outVal));
            }
        }
    }

    @FXML
    private void handleNewSale() {
        if (dashboard != null)
            dashboard.handleVentes();
    }

    @FXML
    private void handleNewOrder() {
        if (dashboard != null)
            dashboard.handleCommandes();
    }

    @FXML
    private void handleGoToProduits() {
        if (dashboard != null)
            dashboard.handleProduits();
    }

    @FXML
    private void handleGoToClients() {
        if (dashboard != null)
            dashboard.handleClients();
    }

    @FXML
    private void handleGoToRapports() {
        if (dashboard != null)
            dashboard.handleRapports();
    }

    @FXML
    private void handleGoToUtilisateurs() {
        if (dashboard != null)
            dashboard.handleUtilisateurs();
    }
}
