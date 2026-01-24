package com.pharmacie.ui.controller;

import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.LogActivite;
import com.pharmacie.model.Utilisateur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsultationLogsController extends BaseController {

    @FXML
    private TableView<LogActivite> tableLogs;
    @FXML
    private TableColumn<LogActivite, Integer> colId;
    @FXML
    private TableColumn<LogActivite, String> colDate; // String for formatted date
    @FXML
    private TableColumn<LogActivite, String> colType;
    @FXML
    private TableColumn<LogActivite, String> colDescription;
    @FXML
    private TableColumn<LogActivite, Integer> colUtilisateur;

    @FXML
    private TextField txtFiltreType;

    private LogActiviteDAO logDAO;
    private ObservableList<LogActivite> logsList = FXCollections.observableArrayList();
    private FilteredList<LogActivite> filteredLogs;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        if (utilisateur != null && !utilisateur.estAdmin()) {
            afficherErreur("Accès Refusé", "Vous n'avez pas les droits d'administrateur.");
            tableLogs.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        logDAO = new LogActiviteDAO();
        setupTable();
        setupSearch();
        chargerLogs();
    }

    private void setupSearch() {
        filteredLogs = new FilteredList<>(logsList, p -> true);

        // Bind the SortedList to the FilteredList
        SortedList<LogActivite> sortedLogs = new SortedList<>(filteredLogs);
        sortedLogs.comparatorProperty().bind(tableLogs.comparatorProperty());
        tableLogs.setItems(sortedLogs);

        txtFiltreType.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLogs.setPredicate(log -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.trim().toLowerCase();

                // Match against Type
                if (log.getTypeAction() != null && log.getTypeAction().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against Description
                if (log.getDescription() != null && log.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against User ID (as string)
                if (log.getIdUtilisateur() != null
                        && String.valueOf(log.getIdUtilisateur()).contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against ID Log
                if (String.valueOf(log.getIdLog()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idLog"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Format date
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateAction();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DATE_FORMATTER) : "");
        });
        colDate.setStyle("-fx-alignment: CENTER;");

        colType.setCellValueFactory(new PropertyValueFactory<>("typeAction"));
        colType.setStyle("-fx-alignment: CENTER;");

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setStyle("-fx-alignment: CENTER;");

        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colUtilisateur.setStyle("-fx-alignment: CENTER;");
    }

    private void chargerLogs() {
        try {
            logsList.setAll(logDAO.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void handleRafraichir() {
        chargerLogs();
    }
}
