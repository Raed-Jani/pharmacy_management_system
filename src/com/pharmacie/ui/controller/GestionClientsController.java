package com.pharmacie.ui.controller;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.model.Client;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.GestionClientService;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

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

    private ClientDAO clientDAO;
    private ObservableList<Client> clientList = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        // Check permissions if needed specific to clients
    }

    @FXML
    public void initialize() {
        try {
            clientDAO = new ClientDAO();
            setupTable();
            chargerClients();
            setupSelectionListener();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur", "Impossible de connecter à la base de données.");
        }
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
                remplirFormulaire(newVal);
            else
                viderFormulaire();
            btnModifier.setDisable(!isSelected);
            btnSupprimer.setDisable(!isSelected);
            btnAjouter.setDisable(isSelected);
        });
    }

    private void chargerClients() {
        try {
            clientList.setAll(clientDAO.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText();
        try {
            if (recherche == null || recherche.trim().isEmpty()) {
                chargerClients();
            } else {
                clientList.setAll(clientDAO.rechercherParNom(recherche));
            }
        } catch (SQLException e) {
            afficherErreur("Erreur recherche", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire())
            return;

        Client nouveau = new Client();
        nouveau.setNom(txtNom.getText());
        nouveau.setPrenom(txtPrenom.getText());
        nouveau.setTelephone(txtTelephone.getText());
        nouveau.setEmail(txtEmail.getText());
        nouveau.setHistoriqueMedical(txtHistorique.getText());

        try {
            if (clientDAO.ajouter(nouveau)) {
                afficherSucces("Client ajouté avec succès");
                chargerClients();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected == null || !validerFormulaire())
            return;

        selected.setNom(txtNom.getText());
        selected.setPrenom(txtPrenom.getText());
        selected.setTelephone(txtTelephone.getText());
        selected.setEmail(txtEmail.getText());
        selected.setHistoriqueMedical(txtHistorique.getText());

        try {
            if (clientDAO.modifier(selected)) {
                afficherSucces("Client modifié avec succès");
                chargerClients();
                tableClients.refresh();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le client ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (clientDAO.supprimer(selected.getIdClient())) {
                    afficherSucces("Client supprimé");
                    chargerClients();
                    viderFormulaire();
                }
            } catch (SQLException e) {
                afficherErreur("Erreur suppression",
                        "Impossible de supprimer (peut-être lié à des ventes ?)\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        tableClients.getSelectionModel().clearSelection();
        viderFormulaire();
    }

    private void remplirFormulaire(Client client) {
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtTelephone.setText(client.getTelephone());
        txtEmail.setText(client.getEmail());
        txtHistorique.setText(client.getHistoriqueMedical());
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtHistorique.clear();
    }

    private boolean validerFormulaire() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty()) {
            afficherErreur("Validation", "Nom et Prénom requis");
            return false;
        }
        return true;
    }
}
