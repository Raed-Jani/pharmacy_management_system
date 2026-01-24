package com.pharmacie.ui.controller;

import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class GestionUtilisateursController extends BaseController {

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;

    @FXML
    private TableColumn<Utilisateur, String> colLogin;
    @FXML
    private TableColumn<Utilisateur, String> colPassword;
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
    private ObservableList<Utilisateur> userList;

    @FXML
    public void initialize() {
        try {
            utilisateurDAO = new UtilisateurDAO();
            userList = FXCollections.observableArrayList();

            setupTable();
            if (cbRole != null) {
                cbRole.getItems().setAll(Utilisateur.ROLE_ADMIN, Utilisateur.ROLE_EMPLOYE);
                cbRole.setValue(Utilisateur.ROLE_EMPLOYE);
            }
            loadUsers();
            setupSelectionListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        if (colLogin != null)
            colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        if (colPassword != null) {
            colPassword.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMotDePasse()));
            colPassword.setVisible(true);
        }
        if (colNom != null)
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        if (colPrenom != null)
            colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        if (colRole != null)
            colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (tableUtilisateurs != null)
            tableUtilisateurs.setItems(userList);
    }

    private void setupSelectionListener() {
        if (tableUtilisateurs == null)
            return;
        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isSelected = newVal != null;
            if (isSelected)
                fillForm(newVal);
            else
                clearForm();
            if (btnModifier != null)
                btnModifier.setDisable(!isSelected);
            if (btnSupprimer != null)
                btnSupprimer.setDisable(!isSelected);
            if (btnAjouter != null)
                btnAjouter.setDisable(isSelected);
        });
    }

    private void loadUsers() {
        try {
            if (utilisateurDAO != null && userList != null) {
                userList.setAll(utilisateurDAO.listerTous());
            }
        } catch (SQLException e) {
            showError("Load Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm())
            return;
        Utilisateur u = getUserFromForm(new Utilisateur());
        try {
            if (utilisateurDAO.ajouter(u)) {
                showSuccess("User added successfully");
                loadUsers();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Add Error", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null || !validateForm())
            return;
        getUserFromForm(selected);
        try {
            if (utilisateurDAO.modifier(selected)) {
                showSuccess("User updated successfully");
                loadUsers();
                tableUtilisateurs.refresh();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Update Error", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (user != null && selected.getIdUtilisateur() == user.getIdUtilisateur()) {
            showError("Action Forbidden", "You cannot delete your own account.");
            return;
        }
        if (confirmDelete("user " + selected.getLogin())) {
            try {
                if (utilisateurDAO.supprimer(selected.getIdUtilisateur())) {
                    showSuccess("User deleted");
                    loadUsers();
                    clearForm();
                }
            } catch (SQLException e) {
                showError("Delete Error", "Could not delete user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        if (tableUtilisateurs != null)
            tableUtilisateurs.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void handleRafraichir() {
        loadUsers();
    }

    private void fillForm(Utilisateur u) {
        if (txtLogin != null)
            txtLogin.setText(u.getLogin());
        if (txtPassword != null) {
            txtPassword.clear();
            txtPassword.setPromptText("Leave blank to keep current");
        }
        if (txtNom != null)
            txtNom.setText(u.getNom());
        if (txtPrenom != null)
            txtPrenom.setText(u.getPrenom());
        if (cbRole != null)
            cbRole.setValue(u.getRole());
    }

    private void clearForm() {
        if (txtLogin != null)
            txtLogin.clear();
        if (txtPassword != null) {
            txtPassword.clear();
            txtPassword.setPromptText("");
        }
        if (txtNom != null)
            txtNom.clear();
        if (txtPrenom != null)
            txtPrenom.clear();
        if (cbRole != null)
            cbRole.setValue(Utilisateur.ROLE_EMPLOYE);
    }

    private boolean validateForm() {
        if (txtLogin == null || txtNom == null)
            return false;
        if (txtLogin.getText().isEmpty() || txtNom.getText().isEmpty()) {
            showError("Validation Error", "Login and Name are required");
            return false;
        }
        if (tableUtilisateurs != null && tableUtilisateurs.getSelectionModel().getSelectedItem() == null
                && txtPassword != null && txtPassword.getText().isEmpty()) {
            showError("Validation Error", "Password is required for new users");
            return false;
        }
        return true;
    }

    private Utilisateur getUserFromForm(Utilisateur u) {
        if (txtLogin != null)
            u.setLogin(txtLogin.getText());
        if (txtPassword != null && !txtPassword.getText().isEmpty())
            u.setMotDePasse(txtPassword.getText());
        if (txtNom != null)
            u.setNom(txtNom.getText());
        if (txtPrenom != null)
            u.setPrenom(txtPrenom.getText());
        if (cbRole != null)
            u.setRole(cbRole.getValue());
        return u;
    }

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        if (utilisateur != null && !utilisateur.estAdmin()) {
            showError("Access Denied", "You do not have administrator privileges.");
            if (tableUtilisateurs != null)
                tableUtilisateurs.setDisable(true);
            if (btnAjouter != null)
                btnAjouter.setDisable(true);
        }
    }
}
