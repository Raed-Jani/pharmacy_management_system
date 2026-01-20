package com.pharmacie.ui.controller;

import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.LogActivite;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

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
        try {
            logDAO = new LogActiviteDAO();
            setupTable();
            chargerLogs();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur", "Impossible de connecter à la base de données.");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idLog"));
        // Format date
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateAction();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.toString().replace("T", " ") : "");
        });
        colType.setCellValueFactory(new PropertyValueFactory<>("typeAction"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));

        tableLogs.setItems(logsList);
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
