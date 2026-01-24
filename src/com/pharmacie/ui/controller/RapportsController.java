package com.pharmacie.ui.controller;

import com.pharmacie.service.GestionVente;
import com.pharmacie.model.Utilisateur;

import com.pharmacie.service.ExportService;
import com.pharmacie.dao.ProduitDAO;
import com.pharmacie.dao.VenteDAO;
import com.pharmacie.model.Vente;
import com.pharmacie.model.Produit;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RapportsController extends BaseController {

    @FXML
    private Label lblTotalVentes;
    @FXML
    private Label lblTotalCA;
    @FXML
    private Label lblTopProduit;
    @FXML
    private ComboBox<String> comboFormat;

    @FXML
    private BarChart<String, Number> barChartCA;

    @FXML
    private PieChart pieChartProduits;

    private GestionVente gestionVente;

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        if (utilisateur != null && !utilisateur.estAdmin()) {
            afficherErreur("Accès Refusé", "Vous n'avez pas les droits d'administrateur.");
        }
    }

    @FXML
    public void initialize() {
        if (barChartCA != null) {
            barChartCA.setAnimated(false);
            CategoryAxis xAxis = (CategoryAxis) barChartCA.getXAxis();
            xAxis.setTickLabelRotation(-45);
        }

        comboFormat.setItems(FXCollections.observableArrayList("PDF", "CSV"));
        comboFormat.setValue("PDF");

        gestionVente = new GestionVente();
        chargerStats();
    }

    private void chargerStats() {
        new Thread(() -> {
            try {
                // KPIs
                double ca = gestionVente.calculerChiffreAffaires();
                int nbVentes = gestionVente.listerVentes().size();
                List<String> topProdStr = gestionVente.getTopProduits(1);
                String bestProduct = topProdStr.isEmpty() ? "-" : topProdStr.get(0).split("\\(")[0].trim();

                // Charts Data
                List<String> caData = gestionVente.getCAJournalier(7);
                List<String> topProductsData = gestionVente.getTopProduits(5);

                Platform.runLater(() -> {
                    lblTotalCA.setText(String.format("%.2f TND", ca));
                    lblTotalVentes.setText(String.valueOf(nbVentes));
                    lblTopProduit.setText(bestProduct);

                    updateBarChart(caData);
                    updatePieChart(topProductsData);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> afficherErreur("Erreur Stats", e.getMessage()));
            }
        }).start();
    }

    private void updateBarChart(List<String> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chiffre d'Affaires");

        if (data != null) {
            for (String entry : data) {
                // Format: "YYYY-MM-DD: 123.45 TND" or "YYYY-MM-DD: 123.45"
                try {
                    String[] parts = entry.split(":");
                    if (parts.length >= 2) {
                        String date = parts[0].trim();
                        // Shorten date from YYYY-MM-DD to MM-DD
                        if (date.length() >= 10) {
                            date = date.substring(5);
                        }
                        String valueStr = parts[1].trim().split(" ")[0].replace(",", ".");
                        double amount = Double.parseDouble(valueStr);
                        series.getData().add(new XYChart.Data<>(date, amount));
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing CA chart data: " + entry + " - " + e.getMessage());
                }
            }
        }

        CategoryAxis xAxis = (CategoryAxis) barChartCA.getXAxis();
        xAxis.getCategories().clear(); // CRITICAL: Clear categories to prevent clumping

        barChartCA.getData().clear();
        barChartCA.getData().add(series);
    }

    private void updatePieChart(List<String> data) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (data != null) {
            // First pass: Calculate total quantity for percentages
            double totalQte = 0;
            List<Object[]> parsedEntries = new ArrayList<>();
            for (String entry : data) {
                try {
                    String nom;
                    int qte;
                    if (entry.contains("(") && entry.contains(")")) {
                        nom = entry.substring(0, entry.lastIndexOf("(")).trim();
                        String qteStr = entry.substring(entry.lastIndexOf("(") + 1, entry.lastIndexOf(")"));
                        qte = Integer.parseInt(qteStr);
                    } else if (entry.contains(":")) {
                        String[] parts = entry.split(":");
                        nom = parts[0].trim();
                        qte = Integer.parseInt(parts[1].trim().split(" ")[0]);
                    } else {
                        continue;
                    }
                    totalQte += qte;
                    parsedEntries.add(new Object[] { nom, qte });
                } catch (Exception e) {
                    System.err.println("Erreur pre-parsing Pie chart: " + entry);
                }
            }

            // Second pass: Create data with percentages
            for (Object[] entry : parsedEntries) {
                String nom = (String) entry[0];
                int qte = (int) entry[1];
                double percentage = (totalQte > 0) ? (qte / totalQte) * 100 : 0;
                String label = String.format("%s (%.1f%%)", nom, percentage);
                pieData.add(new PieChart.Data(label, qte));
            }
        }

        pieChartProduits.setData(pieData);
    }

    @FXML
    private void handleExportProduits() {
        exporterDonnees("produits", (service, path) -> {
            ProduitDAO pdao = new ProduitDAO();
            List<Produit> produits = pdao.listerTous();
            if ("PDF".equals(comboFormat.getValue())) {
                service.exporterProduitsPDF(produits, path);
            } else {
                service.exporterProduits(produits, path);
            }
        });
    }

    @FXML
    private void handleExportVentes() {
        exporterDonnees("ventes", (service, path) -> {
            VenteDAO vdao = new VenteDAO();
            List<Vente> ventes = vdao.listerTous();
            if ("PDF".equals(comboFormat.getValue())) {
                service.exporterVentesPDF(ventes, path);
            } else {
                service.exporterVentes(ventes, path);
            }
        });
    }

    private interface ExportAction {
        void execute(ExportService service, java.nio.file.Path path) throws Exception;
    }

    private void exporterDonnees(String typeName, ExportAction action) {
        String format = comboFormat.getValue();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer l'export des " + typeName + " (" + format + ")");
        chooser.setInitialFileName("export_" + typeName + "." + format.toLowerCase());

        if ("PDF".equals(format)) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        }

        File file = chooser.showSaveDialog(lblTotalCA.getScene().getWindow());

        if (file != null) {
            try {
                ExportService exportService = new ExportService();
                action.execute(exportService, file.toPath());
                afficherSucces("Export Réussi",
                        "Les données (" + typeName + ") ont été exportées en " + format + " vers: "
                                + file.getAbsolutePath());
            } catch (Exception e) {
                afficherErreur("Erreur Export", "Impossible d'exporter en " + format + ": " + e.getMessage());
            }
        }
    }

}
