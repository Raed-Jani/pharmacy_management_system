package com.pharmacie.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.pharmacie.model.*;
import com.pharmacie.service.GestionVente;
import com.pharmacie.service.GestionStock;
import com.pharmacie.dao.ClientDAO;
import com.pharmacie.exception.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour la gestion des ventes.
 */
public class GestionVentesController implements BaseController {

    // ===== Tableau des produits disponibles =====
    @FXML
    private TableView<Produit> tableProduits;

    @FXML
    private TableColumn<Produit, Integer> colProduitId;

    @FXML
    private TableColumn<Produit, String> colProduitNom;

    @FXML
    private TableColumn<Produit, BigDecimal> colProduitPrix;

    @FXML
    private TableColumn<Produit, Integer> colProduitStock;

    // ===== Panier (lignes de vente) =====
    @FXML
    private TableView<LigneVente> tablePanier;

    @FXML
    private TableColumn<LigneVente, String> colPanierProduit;

    @FXML
    private TableColumn<LigneVente, Integer> colPanierQuantite;

    @FXML
    private TableColumn<LigneVente, BigDecimal> colPanierPrix;

    @FXML
    private TableColumn<LigneVente, BigDecimal> colPanierTotal;

    // ===== Champs de saisie =====
    @FXML
    private TextField txtRechercheProduit;

    @FXML
    private TextField txtQuantite;

    @FXML
    private ComboBox<Client> cmbClient;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblStatut;

    @FXML
    private Button btnAjouterPanier;

    @FXML
    private Button btnValiderVente;

    @FXML
    private Button btnAnnuler;

    private Utilisateur utilisateurConnecte;
    private GestionVente gestionVente;
    private GestionStock gestionStock;
    private ClientDAO clientDAO;

    private ObservableList<Produit> produits;
    private ObservableList<LigneVente> panier;
    private BigDecimal totalVente;

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
            gestionVente = new GestionVente();
            gestionStock = new GestionStock();
            clientDAO = new ClientDAO();

            produits = FXCollections.observableArrayList();
            panier = FXCollections.observableArrayList();
            totalVente = BigDecimal.ZERO;

            // Configuration des colonnes produits
            colProduitId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
            colProduitNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colProduitPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
            colProduitStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

            // Configuration des colonnes panier
            colPanierProduit.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomProduit()));
            colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));
            colPanierPrix.setCellValueFactory(new PropertyValueFactory<>("prixApplique"));
            colPanierTotal.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().calculerMontantTotal()));

            // Charger les données
            chargerProduits();
            chargerClients();

            // Configurer les listeners
            tableProduits.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            btnAjouterPanier.setDisable(false);
                        }
                    }
            );

            // Désactiver le bouton de validation au départ
            btnValiderVente.setDisable(true);

        } catch (ConnexionEchoueeException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    /**
     * Charge tous les produits disponibles.
     */
    private void chargerProduits() {
        try {
            produits.clear();
            produits.addAll(gestionStock.listerTousProduits());
            tableProduits.setItems(produits);
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    /**
     * Charge la liste des clients.
     */
    private void chargerClients() {
        try {
            List<Client> clients = clientDAO.listerTous();
            cmbClient.setItems(FXCollections.observableArrayList(clients));

            // Convertir le client en string pour l'affichage
            cmbClient.setCellFactory(param -> new ListCell<Client>() {
                @Override
                protected void updateItem(Client client, boolean empty) {
                    super.updateItem(client, empty);
                    if (empty || client == null) {
                        setText(null);
                    } else {
                        setText(client.getNomComplet());
                    }
                }
            });

            cmbClient.setButtonCell(new ListCell<Client>() {
                @Override
                protected void updateItem(Client client, boolean empty) {
                    super.updateItem(client, empty);
                    if (empty || client == null) {
                        setText("Vente sans client");
                    } else {
                        setText(client.getNomComplet());
                    }
                }
            });

        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les clients");
        }
    }

    /**
     * Ajoute un produit au panier.
     */
    @FXML
    private void handleAjouterPanier() {
        Produit produitSelectionne = tableProduits.getSelectionModel().getSelectedItem();

        if (produitSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un produit");
            return;
        }

        // Valider la quantité
        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite <= 0) {
                afficherErreur("Erreur", "La quantité doit être positive");
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Quantité invalide");
            return;
        }

        // Vérifier le stock disponible
        if (!produitSelectionne.estDisponible(quantite)) {
            afficherErreur(
                    "Stock insuffisant",
                    String.format("Seulement %d unité(s) disponible(s)",
                            produitSelectionne.getQuantiteStock())
            );
            return;
        }

        // Vérifier si le produit est déjà dans le panier
        boolean produitExiste = false;
        for (LigneVente ligne : panier) {
            if (ligne.getIdProduit() == produitSelectionne.getIdProduit()) {
                // Augmenter la quantité
                int nouvelleQuantite = ligne.getQuantiteVendue() + quantite;
                if (!produitSelectionne.estDisponible(nouvelleQuantite)) {
                    afficherErreur("Stock insuffisant",
                            "Stock disponible dépassé avec cette quantité");
                    return;
                }
                ligne.setQuantiteVendue(nouvelleQuantite);
                produitExiste = true;
                break;
            }
        }

        if (!produitExiste) {
            // Créer une nouvelle ligne de vente
            LigneVente ligne = new LigneVente(
                    0, // idVente sera défini à la validation
                    produitSelectionne.getIdProduit(),
                    quantite,
                    produitSelectionne.getPrixUnitaire()
            );
            ligne.setNomProduit(produitSelectionne.getNom());
            panier.add(ligne);
        }

        // Mettre à jour le panier
        tablePanier.setItems(panier);
        calculerTotal();

        // Réinitialiser
        txtQuantite.clear();
        txtQuantite.setText("1");

        // Activer le bouton de validation
        btnValiderVente.setDisable(false);

        afficherSucces("Produit ajouté au panier");
    }

    /**
     * Retire une ligne du panier.
     */
    @FXML
    private void handleRetirerPanier() {
        LigneVente ligneSelectionnee = tablePanier.getSelectionModel().getSelectedItem();

        if (ligneSelectionnee == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner une ligne");
            return;
        }

        panier.remove(ligneSelectionnee);
        calculerTotal();

        if (panier.isEmpty()) {
            btnValiderVente.setDisable(true);
        }

        afficherSucces("Ligne retirée du panier");
    }

    /**
     * Calcule le total de la vente.
     */
    private void calculerTotal() {
        totalVente = BigDecimal.ZERO;
        for (LigneVente ligne : panier) {
            totalVente = totalVente.add(ligne.calculerMontantTotal());
        }
        lblTotal.setText(String.format("%.2f TND", totalVente));
    }

    /**
     * Valide la vente.
     */
    @FXML
    private void handleValiderVente() {
        if (panier.isEmpty()) {
            afficherErreur("Panier vide", "Ajoutez au moins un produit");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Valider la vente");
        alert.setContentText(String.format(
                "Total: %.2f TND\nVoulez-vous confirmer la vente ?",
                totalVente
        ));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                enregistrerVente();
            }
        });
    }

    /**
     * Enregistre la vente dans la base de données.
     */
    private void enregistrerVente() {
        try {
            // Récupérer le client sélectionné (peut être null)
            Client clientSelectionne = cmbClient.getValue();
            Integer idClient = (clientSelectionne != null) ? clientSelectionne.getIdClient() : null;

            // Créer la vente avec toutes les lignes
            List<LigneVente> lignes = new ArrayList<>(panier);
            Vente vente = gestionVente.creerVente(
                    idClient,
                    utilisateurConnecte.getIdUtilisateur(),
                    lignes
            );

            // Afficher le succès
            Alert alertSucces = new Alert(Alert.AlertType.INFORMATION);
            alertSucces.setTitle("Succès");
            alertSucces.setHeaderText("Vente enregistrée");
            alertSucces.setContentText(String.format(
                    "Vente #%d enregistrée avec succès\n" +
                            "Total: %.2f TND\n" +
                            "Nombre d'articles: %d",
                    vente.getIdVente(),
                    vente.getTotalVente(),
                    lignes.size()
            ));
            alertSucces.showAndWait();

            // Réinitialiser
            annulerVente();
            chargerProduits(); // Recharger pour voir les stocks mis à jour

        } catch (StockInsuffisantException e) {
            afficherErreur("Stock insuffisant", e.getMessage());
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible d'enregistrer la vente: " + e.getMessage());
        } catch (ProduitIntrouvableException e) {
            afficherErreur("Erreur", "Produit introuvable: " + e.getMessage());
        }
    }

    /**
     * Annule la vente en cours.
     */
    @FXML
    private void handleAnnuler() {
        if (panier.isEmpty()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler la vente");
        alert.setContentText("Voulez-vous vraiment annuler cette vente ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                annulerVente();
            }
        });
    }

    /**
     * Réinitialise le formulaire de vente.
     */
    private void annulerVente() {
        panier.clear();
        totalVente = BigDecimal.ZERO;
        lblTotal.setText("0.00 TND");
        cmbClient.setValue(null);
        txtQuantite.setText("1");
        btnValiderVente.setDisable(true);
        tableProduits.getSelectionModel().clearSelection();
        lblStatut.setText("Nouvelle vente");
    }

    /**
     * Gère la recherche de produits.
     */
    @FXML
    private void handleRechercherProduit() {
        String recherche = txtRechercheProduit.getText().trim();

        if (recherche.isEmpty()) {
            chargerProduits();
            return;
        }

        try {
            produits.clear();
            produits.addAll(gestionStock.rechercherProduitsParNom(recherche));
            tableProduits.setItems(produits);
        } catch (SQLException e) {
            afficherErreur("Erreur de recherche", e.getMessage());
        }
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