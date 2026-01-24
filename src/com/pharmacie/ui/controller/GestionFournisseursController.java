package com.pharmacie.ui.controller;

import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.model.Fournisseur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

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

    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final ObservableList<Fournisseur> fournisseurList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadFournisseurs();
        setupSelectionListener();
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
                fillForm(newVal);
            else
                clearForm();
            btnModifier.setDisable(!isSelected);
            btnSupprimer.setDisable(!isSelected);
            btnAjouter.setDisable(isSelected);
        });
    }

    private void loadFournisseurs() {
        try {
            fournisseurList.setAll(fournisseurDAO.listerTous());
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    private void handleRechercher() {
        String search = txtRecherche.getText();
        try {
            if (search == null || search.trim().isEmpty()) {
                loadFournisseurs();
            } else {
                fournisseurList.setAll(fournisseurDAO.rechercherParNom(search));
            }
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm())
            return;
        Fournisseur f = getFournisseurFromForm(new Fournisseur());
        try {
            if (fournisseurDAO.ajouter(f)) {
                showSuccess("Supplier added successfully");
                loadFournisseurs();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Add Error", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Fournisseur selected = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selected == null || !validateForm())
            return;
        getFournisseurFromForm(selected);
        try {
            if (fournisseurDAO.modifier(selected)) {
                showSuccess("Supplier updated successfully");
                loadFournisseurs();
                tableFournisseurs.refresh();
                clearForm();
            }
        } catch (SQLException e) {
            showError("Update Error", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Fournisseur selected = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selected != null && confirmDelete(selected.getNomSociete())) {
            try {
                if (fournisseurDAO.supprimer(selected.getIdFournisseur())) {
                    showSuccess("Supplier deleted");
                    loadFournisseurs();
                    clearForm();
                }
            } catch (SQLException e) {
                showError("Delete Error", "Could not delete supplier (likely linked to orders)\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        tableFournisseurs.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void handleRafraichir() {
        loadFournisseurs();
    }

    private void fillForm(Fournisseur f) {
        txtNomSociete.setText(f.getNomSociete());
        txtAdresse.setText(f.getAdresse());
        txtTelephone.setText(f.getTelephone());
        txtEmail.setText(f.getEmail());
    }

    private void clearForm() {
        txtNomSociete.clear();
        txtAdresse.clear();
        txtTelephone.clear();
        txtEmail.clear();
    }

    private boolean validateForm() {
        if (txtNomSociete.getText().isEmpty()) {
            showError("Validation Error", "Company Name is required");
            return false;
        }
        return true;
    }

    private Fournisseur getFournisseurFromForm(Fournisseur f) {
        f.setNomSociete(txtNomSociete.getText());
        f.setAdresse(txtAdresse.getText());
        f.setTelephone(txtTelephone.getText());
        f.setEmail(txtEmail.getText());
        return f;
    }
}
