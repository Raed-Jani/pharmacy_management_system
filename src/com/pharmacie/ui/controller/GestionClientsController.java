package com.pharmacie.ui.controller;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.model.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class GestionClientsController extends BaseController {

    @FXML
    private TableView<Client> tableClients;
    @FXML
    private TableColumn<Client, Integer> colId;
    @FXML
    private TableColumn<Client, String> colNom;
    @FXML
    private TableColumn<Client, String> colPrenom;
    @FXML
    private TableColumn<Client, String> colTel;
    @FXML
    private TableColumn<Client, String> colEmail;
    @FXML
    private TableColumn<Client, String> colHistorique;

    @FXML
    private TextField txtRecherche;
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtHistorique;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    private final ClientDAO clientDAO = new ClientDAO();
    private final ObservableList<Client> clientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadClients();
        setupSelectionListener();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colHistorique.setCellValueFactory(new PropertyValueFactory<>("historiqueMedical"));
        tableClients.setItems(clientList);
    }

    private void setupSelectionListener() {
        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isSelected = newVal != null;
            if (isSelected)
                fillForm(newVal);
            else
                clearForm();
            btnModifier.setDisable(!isSelected);
            btnSupprimer.setDisable(!isSelected);
            btnAjouter.setDisable(isSelected);
        });
    }

    private void loadClients() {
        try {
            clientList.setAll(clientDAO.listerTous());
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    private void handleRechercher() {
        String search = txtRecherche.getText();
        try {
            if (search == null || search.trim().isEmpty()) {
                loadClients();
            } else {
                clientList.setAll(clientDAO.rechercherParNom(search));
            }
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm())
            return;
        Client c = getClientFromForm(new Client());
        try {
            if (clientDAO.ajouter(c)) {
                showSuccess("Client added successfully");
                loadClients();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Add Error", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected == null || !validateForm())
            return;
        getClientFromForm(selected);
        try {
            if (clientDAO.modifier(selected)) {
                showSuccess("Client updated successfully");
                loadClients();
                tableClients.refresh();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Update Error", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null && confirmDelete(selected.getNom() + " " + selected.getPrenom())) {
            try {
                if (clientDAO.supprimer(selected.getIdClient())) {
                    showSuccess("Client deleted");
                    loadClients();
                    clearForm();
                }
            } catch (SQLException e) {
                showError("Delete Error", "Could not delete client (likely linked to sales)\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        tableClients.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void handleRafraichir() {
        loadClients();
    }

    private void fillForm(Client client) {
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtTelephone.setText(client.getTelephone());
        txtEmail.setText(client.getEmail());
        txtHistorique.setText(client.getHistoriqueMedical());
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtHistorique.clear();
    }

    private boolean validateForm() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty()) {
            showError("Validation Error", "Name and First Name are required");
            return false;
        }
        return true;
    }

    private Client getClientFromForm(Client c) {
        c.setNom(txtNom.getText());
        c.setPrenom(txtPrenom.getText());
        c.setTelephone(txtTelephone.getText());
        c.setEmail(txtEmail.getText());
        c.setHistoriqueMedical(txtHistorique.getText());
        return c;
    }
}
