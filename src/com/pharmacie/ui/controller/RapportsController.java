package com.pharmacie.ui.controller;

import com.pharmacie.service.GestionVente;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;

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
import javafx.scene.control.Alert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.SQLException;
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
    private CategoryAxis xAxisCA;
    @FXML
    private NumberAxis yAxisCA;

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
        comboFormat.setItems(FXCollections.observableArrayList("PDF", "CSV"));
        comboFormat.setValue("PDF");

        try {
            gestionVente = new GestionVente();
            chargerStats();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur", "Problème de connexion.");
        }
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

        for (String entry : data) {
            // Expected format "YYYY-MM-DD: 123.45 TND"
            try {
                String[] parts = entry.split(": ");
                String date = parts[0];
                double amount = Double.parseDouble(parts[1].replace(" TND", "").replace(",", "."));
                series.getData().add(new XYChart.Data<>(date, amount));
            } catch (Exception e) {
                System.err.println("Erreur parsing CA chart data: " + entry);
            }
        }

        barChartCA.getData().clear();
        barChartCA.getData().add(series);
    }

    private void updatePieChart(List<String> data) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (String entry : data) {
            // Expected format "Nom (Qté)"
            try {
                String nom = entry.substring(0, entry.lastIndexOf("(")).trim();
                String qteStr = entry.substring(entry.lastIndexOf("(") + 1, entry.lastIndexOf(")"));
                int qte = Integer.parseInt(qteStr);
                pieData.add(new PieChart.Data(nom, qte));
            } catch (Exception e) {
                System.err.println("Erreur parsing Pie chart data: " + entry);
            }
        }

        pieChartProduits.setData(pieData);
    }

    @FXML
    private void handleExportProduits() {
        String format = comboFormat.getValue();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer l'export des produits (" + format + ")");

        String fileName = "export_produits." + format.toLowerCase();
        chooser.setInitialFileName(fileName);

        if ("PDF".equals(format)) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        }

        File file = chooser.showSaveDialog(lblTotalCA.getScene().getWindow());

        if (file != null) {
            try {
                ProduitDAO pdao = new ProduitDAO();
                List<Produit> produits = pdao.listerTous();
                ExportService exportService = new ExportService();

                if ("PDF".equals(format)) {
                    exportService.exporterProduitsPDF(produits, file.toPath());
                } else {
                    exportService.exporterProduits(produits, file.toPath());
                }

                afficherSucces("Export Réussi",
                        "La liste des produits a été exportée en " + format + " vers: " + file.getAbsolutePath());
            } catch (Exception e) {
                afficherErreur("Erreur Export", "Impossible d'exporter en " + format + ": " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportVentes() {
        String format = comboFormat.getValue();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer l'export des ventes (" + format + ")");

        String fileName = "export_ventes." + format.toLowerCase();
        chooser.setInitialFileName(fileName);

        if ("PDF".equals(format)) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        }

        File file = chooser.showSaveDialog(lblTotalCA.getScene().getWindow());

        if (file != null) {
            try {
                VenteDAO vdao = new VenteDAO();
                List<Vente> ventes = vdao.listerTous();
                ExportService exportService = new ExportService();

                if ("PDF".equals(format)) {
                    exportService.exporterVentesPDF(ventes, file.toPath());
                } else {
                    exportService.exporterVentes(ventes, file.toPath());
                }

                afficherSucces("Export Réussi",
                        "Le journal des ventes a été exporté en " + format + " vers: " + file.getAbsolutePath());
            } catch (Exception e) {
                afficherErreur("Erreur Export", "Impossible d'exporter en " + format + ": " + e.getMessage());
            }
        }
    }

}
