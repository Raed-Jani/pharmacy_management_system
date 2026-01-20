package com.pharmacie.ui.controller;

import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Optional;

public class GestionFournisseursController extends BaseController {

    @FXML
    private TableView<Fournisseur> tableFournisseurs;
    @FXML
    private TableColumn<Fournisseur, Integer> colId;
    @FXML
    private TableColumn<Fournisseur, String> colNomSociete;
    @FXML
    private TableColumn<Fournisseur, String> colAdresse;
    @FXML
    private TableColumn<Fournisseur, String> colTelephone;
    @FXML
    private TableColumn<Fournisseur, String> colEmail;

    @FXML
    private TextField txtRecherche;
    @FXML
    private TextField txtNomSociete;
    @FXML
    private TextField txtAdresse;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextField txtEmail;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    private FournisseurDAO fournisseurDAO;
    private ObservableList<Fournisseur> fournisseurList = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
    }

    @FXML
    public void initialize() {
        try {
            fournisseurDAO = new FournisseurDAO();
            setupTable();
            chargerFournisseurs();
            setupSelectionListener();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur", "Impossible de connecter à la base de données.");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFournisseur"));
        colNomSociete.setCellValueFactory(new PropertyValueFactory<>("nomSociete"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableFournisseurs.setItems(fournisseurList);
    }

    private void setupSelectionListener() {
        tableFournisseurs.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
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

    private void chargerFournisseurs() {
        try {
            fournisseurList.setAll(fournisseurDAO.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText();
        try {
            if (recherche == null || recherche.trim().isEmpty()) {
                chargerFournisseurs();
            } else {
                fournisseurList.setAll(fournisseurDAO.rechercherParNom(recherche));
            }
        } catch (SQLException e) {
            afficherErreur("Erreur recherche", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire())
            return;

        Fournisseur nouveau = new Fournisseur();
        nouveau.setNomSociete(txtNomSociete.getText());
        nouveau.setAdresse(txtAdresse.getText());
        nouveau.setTelephone(txtTelephone.getText());
        nouveau.setEmail(txtEmail.getText());

        try {
            if (fournisseurDAO.ajouter(nouveau)) {
                afficherSucces("Fournisseur ajouté avec succès");
                chargerFournisseurs();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Fournisseur selected = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selected == null || !validerFormulaire())
            return;

        selected.setNomSociete(txtNomSociete.getText());
        selected.setAdresse(txtAdresse.getText());
        selected.setTelephone(txtTelephone.getText());
        selected.setEmail(txtEmail.getText());

        try {
            if (fournisseurDAO.modifier(selected)) {
                afficherSucces("Fournisseur modifié avec succès");
                chargerFournisseurs();
                tableFournisseurs.refresh();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Fournisseur selected = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le fournisseur ?");
        alert.setContentText("Cette action est irréversible. Attention si des commandes sont liées.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (fournisseurDAO.supprimer(selected.getIdFournisseur())) {
                    afficherSucces("Fournisseur supprimé");
                    chargerFournisseurs();
                    viderFormulaire();
                }
            } catch (SQLException e) {
                afficherErreur("Erreur suppression",
                        "Impossible de supprimer (probablement lié à des commandes)\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        tableFournisseurs.getSelectionModel().clearSelection();
        viderFormulaire();
    }

    private void remplirFormulaire(Fournisseur f) {
        txtNomSociete.setText(f.getNomSociete());
        txtAdresse.setText(f.getAdresse());
        txtTelephone.setText(f.getTelephone());
        txtEmail.setText(f.getEmail());
    }

    private void viderFormulaire() {
        txtNomSociete.clear();
        txtAdresse.clear();
        txtTelephone.clear();
        txtEmail.clear();
    }

    private boolean validerFormulaire() {
        if (txtNomSociete.getText().isEmpty()) {
            afficherErreur("Validation", "Nom société requis");
            return false;
        }
        return true;
    }
}
