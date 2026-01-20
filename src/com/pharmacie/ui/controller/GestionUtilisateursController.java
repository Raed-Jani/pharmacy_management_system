package com.pharmacie.ui.controller;

import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

public class GestionUtilisateursController extends BaseController {

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colLogin;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colRole;

    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> userList = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        if (utilisateur != null && !utilisateur.estAdmin()) {
            afficherErreur("Accès Refusé", "Vous n'avez pas les droits d'administrateur.");
            // Désactiver l'interface
            tableUtilisateurs.setDisable(true);
            btnAjouter.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        try {
            utilisateurDAO = new UtilisateurDAO();
            setupTable();
            setupForm();
            chargerUtilisateurs();
            setupSelectionListener();
        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur", "Impossible de connecter à la base de données.");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        tableUtilisateurs.setItems(userList);
    }

    private void setupForm() {
        cbRole.getItems().addAll(Utilisateur.ROLE_ADMIN, Utilisateur.ROLE_EMPLOYE);
        cbRole.setValue(Utilisateur.ROLE_EMPLOYE);
    }

    private void setupSelectionListener() {
        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
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

    private void chargerUtilisateurs() {
        try {
            userList.setAll(utilisateurDAO.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire())
            return;

        Utilisateur nouveau = new Utilisateur();
        nouveau.setLogin(txtLogin.getText());
        nouveau.setMotDePasse(txtPassword.getText());
        nouveau.setNom(txtNom.getText());
        nouveau.setPrenom(txtPrenom.getText());
        nouveau.setRole(cbRole.getValue());

        try {
            if (utilisateurDAO.ajouter(nouveau)) {
                afficherSucces("Utilisateur ajouté avec succès");
                chargerUtilisateurs();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null || !validerFormulaire())
            return;

        selected.setLogin(txtLogin.getText());
        // Only update password if not empty
        if (!txtPassword.getText().isEmpty()) {
            selected.setMotDePasse(txtPassword.getText());
        }
        selected.setNom(txtNom.getText());
        selected.setPrenom(txtPrenom.getText());
        selected.setRole(cbRole.getValue());

        try {
            if (utilisateurDAO.modifier(selected)) {
                afficherSucces("Utilisateur modifié avec succès");
                chargerUtilisateurs();
                tableUtilisateurs.refresh();
                viderFormulaire();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        // Prevent deleting yourself
        if (utilisateurConnecte != null && selected.getIdUtilisateur() == utilisateurConnecte.getIdUtilisateur()) {
            afficherErreur("Action interdite", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (utilisateurDAO.supprimer(selected.getIdUtilisateur())) {
                    afficherSucces("Utilisateur supprimé");
                    chargerUtilisateurs();
                    viderFormulaire();
                }
            } catch (SQLException e) {
                afficherErreur("Erreur suppression", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        tableUtilisateurs.getSelectionModel().clearSelection();
        viderFormulaire();
    }

    private void remplirFormulaire(Utilisateur user) {
        txtLogin.setText(user.getLogin());
        txtPassword.clear(); // Security: don't show password
        txtPassword.setPromptText("Laisser vide pour ne pas changer");
        txtNom.setText(user.getNom());
        txtPrenom.setText(user.getPrenom());
        cbRole.setValue(user.getRole());
    }

    private void viderFormulaire() {
        txtLogin.clear();
        txtPassword.clear();
        txtPassword.setPromptText("");
        txtNom.clear();
        txtPrenom.clear();
        cbRole.setValue(Utilisateur.ROLE_EMPLOYE);
    }

    private boolean validerFormulaire() {
        if (txtLogin.getText().isEmpty() || txtNom.getText().isEmpty()) {
            afficherErreur("Validation", "Login et Nom requis");
            return false;
        }
        if (tableUtilisateurs.getSelectionModel().getSelectedItem() == null && txtPassword.getText().isEmpty()) {
            afficherErreur("Validation", "Mot de passe requis pour la création");
            return false;
        }
        return true;
    }

}
