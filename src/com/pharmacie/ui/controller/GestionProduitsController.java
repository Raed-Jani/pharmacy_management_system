package com.pharmacie.ui.controller;

import com.pharmacie.model.Produit;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.service.GestionStock;
import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.exception.ProduitIntrouvableException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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

    private GestionStock gestionStock;
    private FournisseurDAO fournisseurDAO;
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            gestionStock = new GestionStock();
            fournisseurDAO = new FournisseurDAO();
            setupTable();
            chargerProduits();
            chargerFournisseurs();
            setupSearch();

            // Listen to selection
            tableProduits.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                boolean isSelected = newVal != null;
                if (isSelected)
                    remplirFormulaire(newVal);
                else
                    viderFormulaire();
                btnAjouter.setDisable(isSelected);
                btnModifier.setDisable(!isSelected);
                btnSupprimer.setDisable(!isSelected);
            });

        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCodeBarre.setCellValueFactory(new PropertyValueFactory<>("codeBarre"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));

        // Format Date column
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colExpiration.setCellFactory(column -> new TableCell<Produit, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dtf.format(item));
                }
            }
        });

        // Highlight rows with low stock OR expiring soon
        tableProduits.setRowFactory(tv -> new TableRow<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getQuantiteStock() <= 0) {
                    setStyle("-fx-background-color: #ffcccc;"); // Out of stock - Light red
                } else if (item.estEnAlerte()) {
                    setStyle("-fx-background-color: #fff0b3;"); // Low stock - Light orange/yellow
                } else if (item.estProcheExpiration()) {
                    setStyle("-fx-background-color: #e6ccff;"); // Expiring soon - Light purple
                } else {
                    setStyle("");
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

    private void chargerProduits() {
        try {
            produitsList.setAll(gestionStock.listerTousProduits());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    private void chargerFournisseurs() {
        try {
            List<Fournisseur> fournisseurs = fournisseurDAO.listerTous();
            comboFournisseur.setItems(FXCollections.observableArrayList(fournisseurs));
            comboFournisseur.setConverter(new StringConverter<Fournisseur>() {
                @Override
                public String toString(Fournisseur f) {
                    return f == null ? "Sélectionner un fournisseur" : f.getNomSociete();
                }

                @Override
                public Fournisseur fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            System.err.println("Erreur chargement fournisseurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire())
            return;

        try {
            Fournisseur f = comboFournisseur.getValue();
            Produit nouveau = new Produit(
                    txtNom.getText(),
                    txtDescription.getText(),
                    txtCodeBarre.getText(),
                    new BigDecimal(txtPrix.getText()),
                    Integer.parseInt(txtStock.getText()),
                    Integer.parseInt(txtSeuil.getText()),
                    dateExpiration.getValue(),
                    f != null ? f.getIdFournisseur() : null);

            if (gestionStock.ajouterProduit(nouveau, getUtilisateurConnecte().getIdUtilisateur())) {
                afficherSucces("Produit ajouté avec succès");
                chargerProduits();
                viderFormulaire();
            } else {
                afficherErreur("Erreur", "Impossible d'ajouter le produit");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur Format", "Prix ou quantités invalides");
        }
    }

    @FXML
    private void handleModifier() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (!validerFormulaire())
            return;

        try {
            Fournisseur f = comboFournisseur.getValue();
            selected.setNom(txtNom.getText());
            selected.setDescription(txtDescription.getText());
            selected.setCodeBarre(txtCodeBarre.getText());
            selected.setPrixUnitaire(new BigDecimal(txtPrix.getText()));
            selected.setQuantiteStock(Integer.parseInt(txtStock.getText()));
            selected.setSeuilAlerte(Integer.parseInt(txtSeuil.getText()));
            selected.setDateExpiration(dateExpiration.getValue());
            selected.setIdFournisseur(f != null ? f.getIdFournisseur() : null);

            if (gestionStock.modifierProduit(selected, getUtilisateurConnecte().getIdUtilisateur())) {
                afficherSucces("Produit modifié avec succès");
                chargerProduits();
                viderFormulaire();
            } else {
                afficherErreur("Erreur", "Impossible de modifier le produit");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur Format", "Prix ou quantités invalides");
        }
    }

    @FXML
    private void handleSupprimer() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le produit " + selected.getNom() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (gestionStock.supprimerProduit(selected.getIdProduit(),
                        getUtilisateurConnecte().getIdUtilisateur())) {
                    afficherSucces("Produit supprimé");
                    chargerProduits();
                    viderFormulaire();
                } else {
                    afficherErreur("Erreur", "Impossible de supprimer (peut-être lié à des ventes ?)");
                }
            } catch (SQLException e) {
                afficherErreur("Erreur SQL", e.getMessage());
            } catch (ProduitIntrouvableException e) {
                afficherErreur("Erreur", "Produit introuvable");
            }
        }
    }

    @FXML
    private void handleVider() {
        viderFormulaire();
    }

    @FXML
    private void handleRafraichir() {
        chargerProduits();
        chargerFournisseurs();
    }

    private void remplirFormulaire(Produit p) {
        txtNom.setText(p.getNom());
        txtCodeBarre.setText(p.getCodeBarre() != null ? p.getCodeBarre() : "");
        txtPrix.setText(p.getPrixUnitaire().toString());
        txtStock.setText(String.valueOf(p.getQuantiteStock()));
        txtSeuil.setText(String.valueOf(p.getSeuilAlerte()));
        txtDescription.setText(p.getDescription() != null ? p.getDescription() : "");
        dateExpiration.setValue(p.getDateExpiration());

        // Match supplier in combo
        if (p.getIdFournisseur() != null) {
            for (Fournisseur f : comboFournisseur.getItems()) {
                if (f.getIdFournisseur() == p.getIdFournisseur()) {
                    comboFournisseur.setValue(f);
                    break;
                }
            }
        } else {
            comboFournisseur.setValue(null);
        }
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtCodeBarre.clear();
        txtPrix.clear();
        txtStock.clear();
        txtSeuil.clear();
        txtDescription.clear();
        dateExpiration.setValue(null);
        comboFournisseur.setValue(null);
        tableProduits.getSelectionModel().clearSelection();
        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private boolean validerFormulaire() {
        if (txtNom.getText().isEmpty() || txtPrix.getText().isEmpty() || txtStock.getText().isEmpty()) {
            afficherErreur("Erreur", "Nom, Prix et Stock sont obligatoires");
            return false;
        }
        return true;
    }
}
