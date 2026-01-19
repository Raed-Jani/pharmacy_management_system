package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.model.Produit;
import com.pharmacie.service.GestionStock;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.exception.ProduitIntrouvableException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des produits.
 */
public class GestionProduitsController implements BaseController {

    @FXML
    private TableView<Produit> tableProduits;

    @FXML
    private TableColumn<Produit, Integer> colId;

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
    private TextField txtRecherche;

    @FXML
    private TextField txtNom;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtCodeBarre;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtSeuil;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Label lblStatut;

    private Utilisateur utilisateurConnecte;
    private GestionStock gestionStock;
    private ObservableList<Produit> produits;
    private Produit produitSelectionne;

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    /**
     * Initialisation du contrôleur.
     */
    @FXML
    public void initialize() {
        try {
            gestionStock = new GestionStock();
            produits = FXCollections.observableArrayList();

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colCodeBarre.setCellValueFactory(new PropertyValueFactory<>("codeBarre"));
            colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
            colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

            // Colorer les lignes en alerte
            tableProduits.setRowFactory(tv -> new TableRow<Produit>() {
                @Override
                protected void updateItem(Produit produit, boolean empty) {
                    super.updateItem(produit, empty);
                    if (produit == null || empty) {
                        setStyle("");
                    } else if (produit.estEnAlerte()) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else {
                        setStyle("");
                    }
                }
            });

            // Gérer la sélection
            tableProduits.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            afficherDetailsProduit(newSelection);
                        }
                    }
            );

            // Charger les produits
            chargerProduits();

            // Configurer les boutons
            btnModifier.setDisable(true);
            btnSupprimer.setDisable(true);

        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    /**
     * Charge tous les produits.
     */
    private void chargerProduits() {
        try {
            produits.clear();
            produits.addAll(gestionStock.listerTousProduits());
            tableProduits.setItems(produits);
            lblStatut.setText(produits.size() + " produit(s) chargé(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    /**
     * Affiche les détails d'un produit dans le formulaire.
     */
    private void afficherDetailsProduit(Produit produit) {
        produitSelectionne = produit;

        txtNom.setText(produit.getNom());
        txtDescription.setText(produit.getDescription());
        txtCodeBarre.setText(produit.getCodeBarre());
        txtPrix.setText(produit.getPrixUnitaire().toString());
        txtStock.setText(String.valueOf(produit.getQuantiteStock()));
        txtSeuil.setText(String.valueOf(produit.getSeuilAlerte()));

        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
        btnAjouter.setText("Nouveau");
    }

    /**
     * Gère l'ajout d'un produit.
     */
    @FXML
    private void handleAjouter() {
        if (btnAjouter.getText().equals("Nouveau")) {
            viderFormulaire();
            return;
        }

        if (!validerFormulaire()) {
            return;
        }

        try {
            Produit produit = new Produit(
                    txtNom.getText().trim(),
                    txtDescription.getText().trim(),
                    txtCodeBarre.getText().trim(),
                    new BigDecimal(txtPrix.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim()),
                    Integer.parseInt(txtSeuil.getText().trim())
            );

            gestionStock.ajouterProduit(produit, utilisateurConnecte.getIdUtilisateur());

            afficherSucces("Produit ajouté avec succès");
            chargerProduits();
            viderFormulaire();

        } catch (SQLException e) {
            afficherErreur("Erreur d'ajout", e.getMessage());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur de saisie", "Vérifiez le format des nombres");
        }
    }

    /**
     * Gère la modification d'un produit.
     */
    @FXML
    private void handleModifier() {
        if (produitSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un produit");
            return;
        }

        if (!validerFormulaire()) {
            return;
        }

        try {
            produitSelectionne.setNom(txtNom.getText().trim());
            produitSelectionne.setDescription(txtDescription.getText().trim());
            produitSelectionne.setCodeBarre(txtCodeBarre.getText().trim());
            produitSelectionne.setPrixUnitaire(new BigDecimal(txtPrix.getText().trim()));
            produitSelectionne.setQuantiteStock(Integer.parseInt(txtStock.getText().trim()));
            produitSelectionne.setSeuilAlerte(Integer.parseInt(txtSeuil.getText().trim()));

            gestionStock.modifierProduit(produitSelectionne, utilisateurConnecte.getIdUtilisateur());

            afficherSucces("Produit modifié avec succès");
            chargerProduits();
            viderFormulaire();

        } catch (SQLException e) {
            afficherErreur("Erreur de modification", e.getMessage());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur de saisie", "Vérifiez le format des nombres");
        }
    }

    /**
     * Gère la suppression d'un produit.
     */
    @FXML
    private void handleSupprimer() {
        if (produitSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un produit");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le produit");
        alert.setContentText("Voulez-vous vraiment supprimer " + produitSelectionne.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gestionStock.supprimerProduit(
                        produitSelectionne.getIdProduit(),
                        utilisateurConnecte.getIdUtilisateur()
                );

                afficherSucces("Produit supprimé avec succès");
                chargerProduits();
                viderFormulaire();

            } catch (SQLException | ProduitIntrouvableException e) {
                afficherErreur("Erreur de suppression", e.getMessage());
            }
        }
    }

    /**
     * Gère la recherche de produits.
     */
    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText().trim();

        if (recherche.isEmpty()) {
            chargerProduits();
            return;
        }

        try {
            produits.clear();
            produits.addAll(gestionStock.rechercherProduitsParNom(recherche));
            tableProduits.setItems(produits);
            lblStatut.setText(produits.size() + " résultat(s) trouvé(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur de recherche", e.getMessage());
        }
    }

    /**
     * Affiche uniquement les produits en alerte.
     */
    @FXML
    private void handleAlertes() {
        try {
            produits.clear();
            produits.addAll(gestionStock.getProduitsEnAlerte());
            tableProduits.setItems(produits);
            lblStatut.setText(produits.size() + " produit(s) en alerte");
        } catch (SQLException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }

    /**
     * Valide le formulaire.
     */
    private boolean validerFormulaire() {
        if (txtNom.getText().trim().isEmpty()) {
            afficherErreur("Champ requis", "Le nom est obligatoire");
            return false;
        }

        try {
            BigDecimal prix = new BigDecimal(txtPrix.getText().trim());
            if (prix.compareTo(BigDecimal.ZERO) < 0) {
                afficherErreur("Erreur", "Le prix doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Prix invalide");
            return false;
        }

        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            int seuil = Integer.parseInt(txtSeuil.getText().trim());
            if (stock < 0 || seuil < 0) {
                afficherErreur("Erreur", "Les quantités doivent être positives");
                return false;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Quantités invalides");
            return false;
        }

        return true;
    }

    /**
     * Vide le formulaire.
     */
    private void viderFormulaire() {
        produitSelectionne = null;
        txtNom.clear();
        txtDescription.clear();
        txtCodeBarre.clear();
        txtPrix.clear();
        txtStock.clear();
        txtSeuil.clear();

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAjouter.setText("Ajouter");

        tableProduits.getSelectionModel().clearSelection();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String message) {
        lblStatut.setText(message);
        lblStatut.setStyle("-fx-text-fill: green;");
    }
}