package com.pharmacie.ui.controller;

import com.pharmacie.model.CommandeFournisseur;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.model.LigneCommandeFournisseur;
import com.pharmacie.model.Produit;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.GestionCommandesService;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionCommandesController extends BaseController {

    // === TAB 1: NOUVELLE COMMANDE ===
    @FXML
    private ComboBox<Fournisseur> cbFournisseur;
    @FXML
    private ComboBox<Produit> cbProduit;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtPrixAchat; // Prix d'achat unitaire proposé
    @FXML
    private TableView<LigneCommandeFournisseur> tablePanier;
    @FXML
    private TableColumn<LigneCommandeFournisseur, String> colPanierProduit;
    @FXML
    private TableColumn<LigneCommandeFournisseur, Integer> colPanierQuantite;
    @FXML
    private TableColumn<LigneCommandeFournisseur, BigDecimal> colPanierPrix;
    @FXML
    private TableColumn<LigneCommandeFournisseur, BigDecimal> colPanierTotal;
    @FXML
    private Label lblTotalCommande;
    @FXML
    private Button btnAjouterPanier;
    @FXML
    private Button btnValiderCommande;

    // === TAB 2: HISTORIQUE ===
    @FXML
    private TableView<CommandeFournisseur> tableCommandes;
    @FXML
    private TableColumn<CommandeFournisseur, Integer> colCmdId;
    @FXML
    private TableColumn<CommandeFournisseur, String> colCmdDate;
    @FXML
    private TableColumn<CommandeFournisseur, String> colCmdFournisseur;
    @FXML
    private TableColumn<CommandeFournisseur, String> colCmdStatut;
    @FXML
    private Button btnReceptionner;
    @FXML
    private Button btnAnnuler;

    private GestionCommandesService commandesService;
    private ObservableList<LigneCommandeFournisseur> panier = FXCollections.observableArrayList();
    private ObservableList<CommandeFournisseur> historiqueCommandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            commandesService = new GestionCommandesService();

            setupComboBoxes();
            setupTablePanier();
            setupTableHistorique();
            setupListeners();

            chargerHistorique();

        } catch (ConnexionEchoueeException | SQLException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupComboBoxes() throws SQLException {
        // Fournisseurs
        cbFournisseur.setItems(FXCollections.observableArrayList(commandesService.listerFournisseurs()));
        cbFournisseur.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur f) {
                return f != null ? f.getNomSociete() : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return null;
            }
        });

        // Produits
        cbProduit.setItems(FXCollections.observableArrayList(commandesService.listerProduits()));
        cbProduit.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit p) {
                return p != null ? p.getNom() + " (Stock: " + p.getQuantiteStock() + ")" : "";
            }

            @Override
            public Produit fromString(String string) {
                return null;
            }
        });
    }

    private void setupTablePanier() {
        colPanierProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteCommandee"));
        colPanierPrix.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));
        colPanierTotal.setCellValueFactory(new PropertyValueFactory<>("totaleLigne")); // Using pseudo-getter

        tablePanier.setItems(panier);
    }

    private void setupTableHistorique() {
        colCmdId.setCellValueFactory(new PropertyValueFactory<>("idCommande"));
        colCmdDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateCreation() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        colCmdFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));
        colCmdStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tableCommandes.setItems(historiqueCommandes);
    }

    private void setupListeners() {
        // Sélection produit -> préremplir prix
        cbProduit.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                // Par défaut on peut mettre le prix de vente ou 0 si on ne connait pas le PA
                // Ici on met 0 ou on laisse l'utilisateur saisir
                txtQuantite.requestFocus();
            }
        });

        // Activation des boutons historique
        tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isEnCours = newVal != null && newVal.estEnCours();
            btnReceptionner.setDisable(!isEnCours);
            btnAnnuler.setDisable(!isEnCours);
        });

        // Double-clic pour voir détails
        tableCommandes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableCommandes.getSelectionModel().getSelectedItem() != null) {
                handleVoirDetails();
            }
        });
    }

    @FXML
    private void handleRafraichirHistorique() {
        chargerHistorique();
        afficherSucces("Historique actualisé.");
    }

    private void chargerHistorique() {
        try {
            historiqueCommandes.setAll(commandesService.listerCommandes());
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    // === ACTIONS ===

    @FXML
    private void handleNouveauProduit() {
        // Créer un dialogue pour ajouter un nouveau produit
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Produit");
        dialog.setHeaderText("Ajouter un nouveau produit");

        // Boutons
        ButtonType btnAjouter = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAjouter, ButtonType.CANCEL);

        // Créer le formulaire
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom du produit");
        TextField txtDescription = new TextField();
        txtDescription.setPromptText("Description (optionnel)");
        TextField txtCodeBarre = new TextField();
        txtCodeBarre.setPromptText("Code-barres (optionnel)");
        TextField txtPrix = new TextField();
        txtPrix.setPromptText("Prix unitaire");
        TextField txtStock = new TextField();
        txtStock.setPromptText("Quantité en stock");
        txtStock.setText("0");
        TextField txtSeuil = new TextField();
        txtSeuil.setPromptText("Seuil d'alerte");
        txtSeuil.setText("10");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(txtDescription, 1, 1);
        grid.add(new Label("Code-barres:"), 0, 2);
        grid.add(txtCodeBarre, 1, 2);
        grid.add(new Label("Prix unitaire:"), 0, 3);
        grid.add(txtPrix, 1, 3);
        grid.add(new Label("Stock initial:"), 0, 4);
        grid.add(txtStock, 1, 4);
        grid.add(new Label("Seuil d'alerte:"), 0, 5);
        grid.add(txtSeuil, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Focus sur le premier champ
        Platform.runLater(() -> txtNom.requestFocus());

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAjouter) {
                String nom = txtNom.getText().trim();
                String description = txtDescription.getText().trim();
                String codeBarre = txtCodeBarre.getText().trim();
                String prixStr = txtPrix.getText().trim();
                String stockStr = txtStock.getText().trim();
                String seuilStr = txtSeuil.getText().trim();

                // Validation
                if (nom.isEmpty() || prixStr.isEmpty()) {
                    afficherErreur("Validation", "Nom et prix sont obligatoires.");
                    return null;
                }

                try {
                    BigDecimal prix = new BigDecimal(prixStr);
                    int stock = Integer.parseInt(stockStr);
                    int seuil = Integer.parseInt(seuilStr);

                    if (prix.compareTo(BigDecimal.ZERO) < 0 || stock < 0 || seuil < 0) {
                        afficherErreur("Validation", "Les valeurs numériques doivent être positives.");
                        return null;
                    }

                    // Créer le produit
                    Produit produit = new Produit();
                    produit.setNom(nom);
                    produit.setDescription(description.isEmpty() ? null : description);
                    produit.setCodeBarre(codeBarre.isEmpty() ? null : codeBarre);
                    produit.setPrixUnitaire(prix);
                    produit.setQuantiteStock(stock);
                    produit.setSeuilAlerte(seuil);

                    try {
                        // Utiliser ProduitDAO pour ajouter le produit
                        com.pharmacie.dao.ProduitDAO produitDAO = new com.pharmacie.dao.ProduitDAO();
                        boolean success = produitDAO.ajouter(produit);
                        if (success) {
                            afficherSucces("Succès", "Produit ajouté avec succès !");
                            return produit;
                        } else {
                            afficherErreur("Erreur", "Impossible d'ajouter le produit.");
                            return null;
                        }
                    } catch (SQLException e) {
                        afficherErreur("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
                        return null;
                    } catch (ConnexionEchoueeException e) {
                        afficherErreur("Erreur", "Erreur de connexion : " + e.getMessage());
                        return null;
                    }
                } catch (NumberFormatException e) {
                    afficherErreur("Validation", "Prix, stock et seuil doivent être des nombres valides.");
                    return null;
                }
            }
            return null;
        });

        // Afficher le dialogue et traiter le résultat
        Optional<Produit> result = dialog.showAndWait();
        result.ifPresent(produit -> {
            // Recharger la liste des produits
            try {
                cbProduit.setItems(FXCollections.observableArrayList(commandesService.listerProduits()));
                // Sélectionner automatiquement le nouveau produit
                cbProduit.setValue(produit);
            } catch (SQLException e) {
                afficherErreur("Erreur", "Impossible de recharger les produits : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleAjouterPanier() {
        Produit produit = cbProduit.getValue();
        String quantiteStr = txtQuantite.getText();
        String prixStr = txtPrixAchat.getText();

        if (produit == null || quantiteStr.isEmpty() || prixStr.isEmpty()) {
            afficherErreur("Erreur", "Produit, quantité et prix d'achat requis");
            return;
        }

        try {
            int qte = Integer.parseInt(quantiteStr);
            BigDecimal prix = new BigDecimal(prixStr);

            if (qte <= 0 || prix.compareTo(BigDecimal.ZERO) < 0) {
                afficherErreur("Erreur", "Valeurs invalides");
                return;
            }

            LigneCommandeFournisseur ligne = new LigneCommandeFournisseur(produit.getIdProduit(), produit.getNom(), qte,
                    prix);
            panier.add(ligne);
            calculerTotalPanier();

            // Reset fields
            cbProduit.getSelectionModel().clearSelection();
            txtQuantite.clear();
            txtPrixAchat.clear();

        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Format numérique invalide");
        }
    }

    @FXML
    private void handleValiderCommande() {
        Fournisseur fournisseur = cbFournisseur.getValue();
        if (fournisseur == null) {
            afficherErreur("Erreur", "Veuillez sélectionner un fournisseur");
            return;
        }
        if (panier.isEmpty()) {
            afficherErreur("Erreur", "Le panier est vide");
            return;
        }

        try {
            CommandeFournisseur commande = new CommandeFournisseur(
                    CommandeFournisseur.STATUT_EN_COURS,
                    fournisseur.getIdFournisseur());

            commandesService.creerCommande(commande, new ArrayList<>(panier));

            afficherSucces("Commande créée avec succès !");
            panier.clear();
            calculerTotalPanier();
            cbFournisseur.getSelectionModel().clearSelection();
            chargerHistorique(); // Refresh tab 2

        } catch (SQLException | ConnexionEchoueeException e) {
            afficherErreur("Erreur validation", e.getMessage());
        }
    }

    @FXML
    private void handleReceptionner() {
        CommandeFournisseur selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de réception");
        alert.setHeaderText("Réceptionner la commande #" + selected.getIdCommande() + " ?");
        alert.setContentText("Cela mettra à jour le stock des produits concernés.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandesService.receptionnerCommande(selected.getIdCommande());
                afficherSucces("Commande réceptionnée et stock mis à jour !");
                chargerHistorique();
            } catch (SQLException | ConnexionEchoueeException e) {
                afficherErreur("Erreur réception", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        CommandeFournisseur selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Annulation");
        alert.setHeaderText("Annuler la commande #" + selected.getIdCommande() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandesService.annulerCommande(selected.getIdCommande());
                afficherSucces("Commande annulée.");
                chargerHistorique();
            } catch (SQLException | ConnexionEchoueeException e) {
                afficherErreur("Erreur annulation", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimerLignePanier() {
        LigneCommandeFournisseur selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            panier.remove(selected);
            calculerTotalPanier();
        }
    }

    private void calculerTotalPanier() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneCommandeFournisseur l : panier) {
            total = total.add(l.getTotaleLigne());
        }
        lblTotalCommande.setText(String.format("%.2f TND", total));
    }

    @FXML
    private void handleVoirDetails() {
        CommandeFournisseur selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            List<LigneCommandeFournisseur> lignes = commandesService.getDetailsCommande(selected.getIdCommande());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Détails Commande #" + selected.getIdCommande());
            dialog.setHeaderText("Fournisseur: " + selected.getNomFournisseur());

            ButtonType btnFermer = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(btnFermer);

            TableView<LigneCommandeFournisseur> tableDetails = new TableView<>();
            TableColumn<LigneCommandeFournisseur, String> colProd = new TableColumn<>("Produit");
            colProd.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));

            TableColumn<LigneCommandeFournisseur, Integer> colQte = new TableColumn<>("Quantité");
            colQte.setCellValueFactory(new PropertyValueFactory<>("quantiteCommandee"));

            TableColumn<LigneCommandeFournisseur, BigDecimal> colPrix = new TableColumn<>("Prix Achat");
            colPrix.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));

            tableDetails.getColumns().addAll(colProd, colQte, colPrix);
            tableDetails.setItems(FXCollections.observableList(lignes));
            tableDetails.setPrefWidth(400);

            dialog.getDialogPane().setContent(tableDetails);
            dialog.showAndWait();

        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les détails : " + e.getMessage());
        }
    }

    @Override
    public void setUtilisateur(Utilisateur utilisateur) {
        super.setUtilisateur(utilisateur);
        // Les admins ont accès complet, les employés peuvent voir/créer
    }
}
