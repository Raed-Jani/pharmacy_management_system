package com.pharmacie.ui.controller;

import com.pharmacie.model.Produit;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.service.GestionStock;
import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.exception.ProduitIntrouvableException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionProduitsController extends BaseController {

    @FXML
    private TableView<Produit> tableProduits;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, String> colCodeBarre;
    @FXML
    private TableColumn<Produit, BigDecimal> colPrix;
    @FXML
    private TableColumn<Produit, Integer> colStock;
    @FXML
    private TableColumn<Produit, Integer> colSeuil;
    @FXML
    private TableColumn<Produit, LocalDate> colExpiration;
    @FXML
    private TableColumn<Produit, String> colFournisseur;

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtCodeBarre;
    @FXML
    private TextField txtPrix;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtSeuil;
    @FXML
    private DatePicker dateExpiration;
    @FXML
    private ComboBox<Fournisseur> comboFournisseur;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtRecherche;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    private final GestionStock gestionStock = new GestionStock();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();

        tableProduits.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            if (isSelected)
                fillForm(newVal);
            else
                clearForm();
            btnAjouter.setDisable(isSelected);
            btnModifier.setDisable(!isSelected);
            btnSupprimer.setDisable(!isSelected);
        });
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCodeBarre.setCellValueFactory(new PropertyValueFactory<>("codeBarre"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colExpiration.setCellFactory(column -> new TableCell<Produit, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : dtf.format(item));
            }
        });

        tableProduits.setRowFactory(tv -> new TableRow<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                // Clear previous state classes
                getStyleClass().removeAll("row-out-of-stock", "row-alert", "row-expired");
                setStyle(""); // Clear inline styles

                if (item == null || empty) {
                    // Do nothing
                } else if (item.getQuantiteStock() <= 0) {
                    getStyleClass().add("row-out-of-stock");
                } else if (item.estEnAlerte()) {
                    getStyleClass().add("row-alert");
                } else if (item.estProcheExpiration()) {
                    getStyleClass().add("row-expired");
                }
            }
        });
        tableProduits.setItems(produitsList);
    }

    private void setupSearch() {
        txtRecherche.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) {
                tableProduits.setItems(produitsList);
            } else {
                String search = val.toLowerCase();
                tableProduits.setItems(produitsList.filtered(p -> p.getNom().toLowerCase().contains(search) ||
                        (p.getCodeBarre() != null && p.getCodeBarre().contains(val))));
            }
        });
    }

    private void loadData() {
        try {
            produitsList.setAll(gestionStock.listerTousProduits());
            List<Fournisseur> fournisseurs = fournisseurDAO.listerTous();
            comboFournisseur.setItems(FXCollections.observableArrayList(fournisseurs));
            comboFournisseur.setConverter(new StringConverter<Fournisseur>() {
                @Override
                public String toString(Fournisseur f) {
                    return f == null ? "Select Supplier" : f.getNomSociete();
                }

                @Override
                public Fournisseur fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showError("Load Error", "Failed to load products/suppliers: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm())
            return;
        try {
            Produit nouveau = getProduitFromForm();
            if (gestionStock.ajouterProduit(nouveau, user.getIdUtilisateur())) {
                showSuccess("Product added successfully");
                loadData();
                clearForm();
            } else {
                showError("Error", "Could not add product");
            }
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null || !validateForm())
            return;
        try {
            updateProduitFromForm(selected);
            if (gestionStock.modifierProduit(selected, user.getIdUtilisateur())) {
                showSuccess("Product updated successfully");
                loadData();
                clearForm();
            } else {
                showError("Error", "Could not update product");
            }
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected != null && confirmDelete(selected.getNom())) {
            try {
                if (gestionStock.supprimerProduit(selected.getIdProduit(), user.getIdUtilisateur())) {
                    showSuccess("Product deleted");
                    loadData();
                    clearForm();
                } else {
                    showError("Error", "Could not delete product (likely linked to sales)");
                }
            } catch (SQLException | ProduitIntrouvableException e) {
                showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleVider() {
        clearForm();
    }

    @FXML
    private void handleRafraichir() {
        loadData();
    }

    private void fillForm(Produit p) {
        txtNom.setText(p.getNom());
        txtCodeBarre.setText(p.getCodeBarre() != null ? p.getCodeBarre() : "");
        txtPrix.setText(p.getPrixUnitaire().toString());
        txtStock.setText(String.valueOf(p.getQuantiteStock()));
        txtSeuil.setText(String.valueOf(p.getSeuilAlerte()));
        txtDescription.setText(p.getDescription() != null ? p.getDescription() : "");
        dateExpiration.setValue(p.getDateExpiration());
        if (p.getIdFournisseur() != null) {
            comboFournisseur.getItems().stream()
                    .filter(f -> f.getIdFournisseur() == p.getIdFournisseur())
                    .findFirst().ifPresent(comboFournisseur::setValue);
        } else {
            comboFournisseur.setValue(null);
        }
    }

    private void clearForm() {
        txtNom.clear();
        txtCodeBarre.clear();
        txtPrix.clear();
        txtStock.clear();
        txtSeuil.clear();
        txtDescription.clear();
        dateExpiration.setValue(null);
        comboFournisseur.setValue(null);
        tableProduits.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (txtNom.getText().isEmpty() || txtPrix.getText().isEmpty() || txtStock.getText().isEmpty()) {
            showError("Validation Error", "Name, Price, and Stock are required");
            return false;
        }
        return true;
    }

    private Produit getProduitFromForm() {
        Fournisseur f = comboFournisseur.getValue();
        return new Produit(
                txtNom.getText(), txtDescription.getText(), txtCodeBarre.getText(),
                new BigDecimal(txtPrix.getText()), Integer.parseInt(txtStock.getText()),
                Integer.parseInt(txtSeuil.getText()), dateExpiration.getValue(),
                f != null ? f.getIdFournisseur() : null);
    }

    private void updateProduitFromForm(Produit p) {
        Fournisseur f = comboFournisseur.getValue();
        p.setNom(txtNom.getText());
        p.setDescription(txtDescription.getText());
        p.setCodeBarre(txtCodeBarre.getText());
        p.setPrixUnitaire(new BigDecimal(txtPrix.getText()));
        p.setQuantiteStock(Integer.parseInt(txtStock.getText()));
        p.setSeuilAlerte(Integer.parseInt(txtSeuil.getText()));
        p.setDateExpiration(dateExpiration.getValue());
        p.setIdFournisseur(f != null ? f.getIdFournisseur() : null);
    }
}
