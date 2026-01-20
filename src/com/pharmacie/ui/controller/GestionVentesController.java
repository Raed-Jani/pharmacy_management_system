package com.pharmacie.ui.controller;

import com.pharmacie.model.Client;
import com.pharmacie.model.LigneVente;
import com.pharmacie.model.Produit;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.model.Vente;
import com.pharmacie.service.GestionVente;
import com.pharmacie.dao.ClientDAO;
import com.pharmacie.dao.ProduitDAO;
import com.pharmacie.exception.ConnexionEchoueeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;

import javafx.scene.layout.VBox; // Import ajouté pour vider le panier visuellement si besoin

public class GestionVentesController extends BaseController {

    @FXML
    private Label lblDate; // Ajout du label date

    @FXML
    private TableView<Produit> tableProduits;
    @FXML
    private TableColumn<Produit, String> colNomProduit;
    @FXML
    private TableColumn<Produit, BigDecimal> colPrixProduit;
    @FXML
    private TableColumn<Produit, Integer> colStockProduit;

    @FXML
    private TextField fieldRechercheProduit;
    @FXML
    private Spinner<Integer> spinnerQuantite;

    @FXML
    private TableView<LigneVente> tablePanier;
    @FXML
    private TableColumn<LigneVente, String> colPanierProduit;
    @FXML
    private TableColumn<LigneVente, Integer> colPanierQuantite;
    @FXML
    private TableColumn<LigneVente, BigDecimal> colPanierPrix;
    @FXML
    private TableColumn<LigneVente, String> colPanierTotal;

    @FXML
    private ComboBox<Client> comboClient;
    @FXML
    private Label labelTotal;
    @FXML
    private Button btnValiderVente;

    // Historique tab fields
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private ComboBox<Client> comboClientRecherche;
    @FXML
    private TableView<Vente> tableHistoriqueVentes;
    @FXML
    private TableColumn<Vente, Integer> colHistoId;
    @FXML
    private TableColumn<Vente, LocalDateTime> colHistoDate;
    @FXML
    private TableColumn<Vente, String> colHistoClient;
    @FXML
    private TableColumn<Vente, BigDecimal> colHistoTotal;
    @FXML
    private TableColumn<Vente, String> colHistoUtilisateur;
    @FXML
    private Button btnVoirDetails;
    @FXML
    private Button btnSupprimerVente;

    private ProduitDAO produitDAO;
    private ClientDAO clientDAO;
    private GestionVente gestionVente;

    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private ObservableList<LigneVente> panierList = FXCollections.observableArrayList();
    private FilteredList<Produit> filteredProduits;
    private ObservableList<Vente> historiqueVentesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            produitDAO = new ProduitDAO();
            clientDAO = new ClientDAO();
            gestionVente = new GestionVente();

            setupTables();
            loadClients();
            loadProduits();
            setupRecherche();

            // Initialiser le spinner
            spinnerQuantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

            // Mettre à jour la date
            if (lblDate != null) {
                lblDate.setText(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(java.time.LocalDate.now()));
            }

            // Setup historique tab
            setupHistoriqueTab();

        } catch (Exception e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupTables() {
        // Table Produits
        colNomProduit.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrixProduit.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStockProduit.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        // Table Panier
        colPanierProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));
        colPanierPrix.setCellValueFactory(new PropertyValueFactory<>("prixApplique"));
        // Pour le total, on utilise une cellFactory ou un binding simple si possible,
        // mais ici on va utiliser une méthode simple ou le getter formaté
        colPanierTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMontantTotalFormate()));

        tablePanier.setItems(panierList);
    }

    private void setupRecherche() {
        filteredProduits = new FilteredList<>(produitsList, p -> true);
        tableProduits.setItems(filteredProduits);

        fieldRechercheProduit.textProperty().addListener((obs, old, val) -> {
            filteredProduits.setPredicate(p -> val == null || val.isEmpty() ||
                    p.getNom().toLowerCase().contains(val.toLowerCase()) || p.getCodeBarre().contains(val));
        });
    }

    private void loadClients() {
        try {
            List<Client> clients = clientDAO.listerTous();
            comboClient.setItems(FXCollections.observableArrayList(clients));

            // Convertisseur pour afficher le nom du client
            comboClient.setConverter(new StringConverter<Client>() {
                @Override
                public String toString(Client client) {
                    return client == null ? "" : client.getNom() + " " + client.getPrenom();
                }

                @Override
                public Client fromString(String string) {
                    return null; // Pas utilisé
                }
            });
        } catch (Exception e) {
            afficherErreur("Erreur chargement clients", e.getMessage());
        }
    }

    private void loadProduits() {
        try {
            produitsList.setAll(produitDAO.listerTous());
        } catch (Exception e) {
            afficherErreur("Erreur chargement produits", e.getMessage());
        }
    }

    @FXML
    private void actionAjouterPanier() {
        Produit selectedProduit = tableProduits.getSelectionModel().getSelectedItem();
        if (selectedProduit == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un produit.");
            return;
        }

        int quantiteDemandee = spinnerQuantite.getValue();

        // Vérifier stock
        if (selectedProduit.getQuantiteStock() < quantiteDemandee) {
            afficherAlerte(Alert.AlertType.ERROR, "Stock insuffisant",
                    "Stock disponible: " + selectedProduit.getQuantiteStock());
            return;
        }

        // Vérifier si déjà dans le panier
        for (LigneVente ligne : panierList) {
            if (ligne.getIdProduit() == selectedProduit.getIdProduit()) {
                if (ligne.getQuantiteVendue() + quantiteDemandee > selectedProduit.getQuantiteStock()) {
                    afficherAlerte(Alert.AlertType.ERROR, "Stock insuffisant",
                            "Vous avez déjà ce produit dans le panier. Quantité totale impossible.");
                    return;
                }
                ligne.setQuantiteVendue(ligne.getQuantiteVendue() + quantiteDemandee);
                tablePanier.refresh();
                updateTotal();
                return;
            }
        }

        // Nouveau produit dans le panier
        LigneVente ligne = new LigneVente();
        ligne.setIdProduit(selectedProduit.getIdProduit());
        ligne.setNomProduit(selectedProduit.getNom());
        ligne.setPrixApplique(selectedProduit.getPrixUnitaire());
        ligne.setQuantiteVendue(quantiteDemandee);

        panierList.add(ligne);
        updateTotal();
    }

    @FXML
    private void actionRetirerPanier() {
        LigneVente selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            panierList.remove(selected);
            updateTotal();
        }
    }

    @FXML
    private void actionViderPanier() {
        panierList.clear();
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneVente ligne : panierList) {
            total = total.add(ligne.calculerMontantTotal());
        }
        labelTotal.setText(String.format("%.2f TND", total));
        btnValiderVente.setDisable(panierList.isEmpty());
    }

    @FXML
    private void handleNouveauClient() {
        // Créer un dialogue personnalisé pour ajouter un client
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Client");
        dialog.setHeaderText("Ajouter un nouveau client");

        // Boutons
        ButtonType btnAjouter = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAjouter, ButtonType.CANCEL);

        // Créer le formulaire
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom");
        TextField txtPrenom = new TextField();
        txtPrenom.setPromptText("Prénom");
        TextField txtTelephone = new TextField();
        txtTelephone.setPromptText("Téléphone");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email (optionnel)");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(txtPrenom, 1, 1);
        grid.add(new Label("Téléphone:"), 0, 2);
        grid.add(txtTelephone, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(txtEmail, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Focus sur le premier champ
        javafx.application.Platform.runLater(() -> txtNom.requestFocus());

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAjouter) {
                String nom = txtNom.getText().trim();
                String prenom = txtPrenom.getText().trim();
                String telephone = txtTelephone.getText().trim();
                String email = txtEmail.getText().trim();

                // Validation
                if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty()) {
                    afficherErreur("Validation", "Nom, prénom et téléphone sont obligatoires.");
                    return null;
                }

                // Créer le client
                Client client = new Client();
                client.setNom(nom);
                client.setPrenom(prenom);
                client.setTelephone(telephone);
                client.setEmail(email.isEmpty() ? null : email);
                client.setHistoriqueMedical(null); // Pas d'historique médical lors de la création rapide

                try {
                    boolean success = clientDAO.ajouter(client);
                    if (success) {
                        afficherSucces("Succès", "Client ajouté avec succès !");
                        return client;
                    } else {
                        afficherErreur("Erreur", "Impossible d'ajouter le client.");
                        return null;
                    }
                } catch (Exception e) {
                    afficherErreur("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Afficher le dialogue et traiter le résultat
        Optional<Client> result = dialog.showAndWait();
        result.ifPresent(client -> {
            // Recharger la liste des clients
            loadClients();
            // Sélectionner automatiquement le nouveau client
            comboClient.setValue(client);
        });
    }

    @FXML
    private void actionValiderVente() {
        if (panierList.isEmpty()) {
            return;
        }

        try {
            Client client = comboClient.getValue();
            Integer idClient = (client != null) ? client.getIdClient() : null;

            // Récupérer l'utilisateur connecté via BaseController
            if (utilisateurConnecte == null) {
                afficherErreur("Erreur", "Aucun utilisateur connecté.");
                return;
            }

            Vente vente = gestionVente.creerVente(idClient, utilisateurConnecte.getIdUtilisateur(),
                    new ArrayList<>(panierList));

            afficherInformation("Succès", "Vente validée avec succès! Total: " + vente.getTotalVente() + " TND");

            panierList.clear();
            updateTotal();
            loadProduits(); // Recharger pour mettre à jour les stocks affichés

        } catch (java.sql.SQLException e) {
            afficherErreur("Erreur base de données", "Impossible de valider la vente. Détails: " + e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur validation", e.getMessage());
        }
    }

    // ========== HISTORIQUE TAB METHODS ==========

    private void setupHistoriqueTab() {
        if (tableHistoriqueVentes == null)
            return;

        // Setup table columns
        colHistoId.setCellValueFactory(new PropertyValueFactory<>("idVente"));
        colHistoDate.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        colHistoTotal.setCellValueFactory(new PropertyValueFactory<>("totalVente"));
        colHistoUtilisateur.setCellValueFactory(cellData -> {
            String login = cellData.getValue().getLoginUtilisateur();
            return new javafx.beans.property.SimpleStringProperty(login != null ? login : "N/A");
        });
        colHistoClient.setCellValueFactory(cellData -> {
            String nomClient = cellData.getValue().getNomClient();
            return new javafx.beans.property.SimpleStringProperty(nomClient != null ? nomClient : "Client de passage");
        });

        // Format date column
        colHistoDate.setCellFactory(column -> new TableCell<Vente, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(item));
            }
        });

        // Format total column
        colHistoTotal.setCellFactory(column -> new TableCell<Vente, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        tableHistoriqueVentes.setItems(historiqueVentesList);

        // Setup selection listener
        tableHistoriqueVentes.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    if (btnVoirDetails != null)
                        btnVoirDetails.setDisable(!hasSelection);
                    if (btnSupprimerVente != null)
                        btnSupprimerVente.setDisable(!hasSelection);
                });

        // Load clients for search combo
        if (comboClientRecherche != null) {
            try {
                ObservableList<Client> clients = FXCollections.observableArrayList(clientDAO.listerTous());
                clients.add(0, null); // Add "All clients" option
                comboClientRecherche.setItems(clients);
                comboClientRecherche.setConverter(new StringConverter<Client>() {
                    @Override
                    public String toString(Client client) {
                        return client == null ? "Tous les clients" : client.getNom() + " " + client.getPrenom();
                    }

                    @Override
                    public Client fromString(String string) {
                        return null;
                    }
                });
            } catch (Exception e) {
                System.err.println("Erreur chargement clients: " + e.getMessage());
            }
        }

        // Load initial data
        chargerHistoriqueVentes();
    }

    private void chargerHistoriqueVentes() {
        try {
            List<Vente> ventes = gestionVente.listerVentes();
            historiqueVentesList.setAll(ventes);
        } catch (Exception e) {
            afficherErreur("Erreur chargement", "Impossible de charger l'historique: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherVentes() {
        try {
            java.time.LocalDate debut = (dateDebut != null) ? dateDebut.getValue() : null;
            java.time.LocalDate fin = (dateFin != null) ? dateFin.getValue() : null;
            Client client = (comboClientRecherche != null) ? comboClientRecherche.getValue() : null;

            List<Vente> ventes = gestionVente.listerVentes();
            List<Vente> filtered = new ArrayList<>();

            for (Vente v : ventes) {
                boolean match = true;

                // Filter by date range
                if (debut != null && v.getDateVente().toLocalDate().isBefore(debut)) {
                    match = false;
                }
                if (fin != null && v.getDateVente().toLocalDate().isAfter(fin)) {
                    match = false;
                }

                // Filter by client
                if (client != null && v.getIdClient() != null && !v.getIdClient().equals(client.getIdClient())) {
                    match = false;
                }

                if (match) {
                    filtered.add(v);
                }
            }

            historiqueVentesList.setAll(filtered);
        } catch (Exception e) {
            afficherErreur("Erreur recherche", e.getMessage());
        }
    }

    @FXML
    private void handleResetRecherche() {
        if (dateDebut != null)
            dateDebut.setValue(null);
        if (dateFin != null)
            dateFin.setValue(null);
        if (comboClientRecherche != null)
            comboClientRecherche.setValue(null);
        chargerHistoriqueVentes();
    }

    @FXML
    private void handleActualiserHistorique() {
        chargerHistoriqueVentes();
    }

    @FXML
    private void handleVoirDetails() {
        Vente selected = tableHistoriqueVentes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            List<LigneVente> lignes = gestionVente.getLignesVente(selected.getIdVente());

            StringBuilder details = new StringBuilder();
            details.append("Vente N°").append(selected.getIdVente()).append("\n");
            details.append("Date: ").append(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(selected.getDateVente()))
                    .append("\n");
            details.append("Caissier: ").append(selected.getLoginUtilisateur()).append("\n");
            details.append("Client: ")
                    .append(selected.getNomClient() != null ? selected.getNomClient() : "Client de passage")
                    .append("\n\n");
            details.append("Détails:\n");
            details.append("─".repeat(50)).append("\n");

            for (LigneVente ligne : lignes) {
                details.append(String.format("• %s\n", ligne.getNomProduit()));
                details.append(String.format("  Qté: %d × %.2f TND = %.2f TND\n",
                        ligne.getQuantiteVendue(), ligne.getPrixApplique(), ligne.calculerMontantTotal()));
            }

            details.append("─".repeat(50)).append("\n");
            details.append(String.format("TOTAL: %.2f TND", selected.getTotalVente()));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de la vente");
            alert.setHeaderText("Vente N°" + selected.getIdVente());
            alert.setContentText(details.toString());
            alert.showAndWait();

        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de charger les détails: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerVente() {
        Vente selected = tableHistoriqueVentes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la vente ?");
        confirm.setContentText("Vente N°" + selected.getIdVente() + " - Total: " + selected.getTotalVente()
                + " TND\n\nCette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Note: This would need a delete method in GestionVente service
                // For now, we'll show a message
                afficherInformation("Information",
                        "La suppression des ventes nécessite l'implémentation d'une méthode de suppression dans le service.\n\nPour l'instant, cette fonctionnalité est désactivée pour préserver l'intégrité des données.");

                // TODO: Implement soft delete in GestionVente service
                // gestionVente.supprimerVente(selected.getIdVente());
                // chargerHistoriqueVentes();

            } catch (Exception e) {
                afficherErreur("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }
}
