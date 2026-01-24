package com.pharmacie.ui.controller;

import com.pharmacie.model.*;
import com.pharmacie.service.GestionCommande;
import com.pharmacie.dao.ProduitDAO;
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

    @FXML
    private ComboBox<Fournisseur> cbFournisseur;
    @FXML
    private ComboBox<Produit> cbProduit;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtPrixAchat;
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
    private Label lblNbArticles;
    @FXML
    private Button btnAjouterPanier;
    @FXML
    private Button btnValiderCommande;

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

    private final GestionCommande commandesService = new GestionCommande();
    private final ObservableList<LigneCommandeFournisseur> panier = FXCollections.observableArrayList();
    private final ObservableList<CommandeFournisseur> historiqueCommandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTables();
        setupListeners();
        loadHistory();
    }

    private void setupComboBoxes() {
        try {
            cbFournisseur.setItems(FXCollections.observableArrayList(commandesService.listerFournisseurs()));
            cbFournisseur.setConverter(new StringConverter<Fournisseur>() {
                @Override
                public String toString(Fournisseur f) {
                    return f != null ? f.getNomSociete() : "";
                }

                @Override
                public Fournisseur fromString(String s) {
                    return null;
                }
            });

            cbProduit.setItems(FXCollections.observableArrayList(commandesService.listerProduits()));
            cbProduit.setConverter(new StringConverter<Produit>() {
                @Override
                public String toString(Produit p) {
                    return p != null ? p.getNom() + " (Stock: " + p.getQuantiteStock() + ")" : "";
                }

                @Override
                public Produit fromString(String s) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showError("Init Error", e.getMessage());
        }
    }

    private void setupTables() {
        colPanierProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteCommandee"));
        colPanierPrix.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));
        colPanierTotal.setCellValueFactory(new PropertyValueFactory<>("totaleLigne"));
        tablePanier.setItems(panier);

        colCmdId.setCellValueFactory(new PropertyValueFactory<>("idCommande"));
        colCmdDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateCreation() != null
                ? d.getValue().getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : ""));
        colCmdFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));
        colCmdStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        tableCommandes.setItems(historiqueCommandes);
    }

    private void setupListeners() {
        tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isEnCours = newVal != null && newVal.estEnCours();
            btnReceptionner.setDisable(!isEnCours);
            btnAnnuler.setDisable(!isEnCours);
        });
        tableCommandes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableCommandes.getSelectionModel().getSelectedItem() != null)
                handleVoirDetails();
        });
    }

    private void loadHistory() {
        try {
            historiqueCommandes.setAll(commandesService.listerCommandes());
        } catch (SQLException e) {
            showError("History Error", e.getMessage());
        }
    }

    @FXML
    private void handleRafraichirHistorique() {
        loadHistory();
        showSuccess("History refreshed.");
    }

    @FXML
    private void handleRafraichir() {
        loadHistory();
        setupComboBoxes();
    }

    @FXML
    private void handleNouveauProduit() {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("New Product");
        ButtonType btnAdd = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        TextField txtNom = new TextField();
        TextField txtPrix = new TextField();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(new Label("Sell Price:"), 0, 1);
        grid.add(txtPrix, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == btnAdd) {
                Produit p = new Produit();
                p.setNom(txtNom.getText());
                p.setPrixUnitaire(new BigDecimal(txtPrix.getText()));
                p.setQuantiteStock(0);
                p.setSeuilAlerte(10);
                try {
                    if (new ProduitDAO().ajouter(p))
                        return p;
                } catch (SQLException e) {
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                cbProduit.setItems(FXCollections.observableArrayList(commandesService.listerProduits()));
                cbProduit.setValue(p);
            } catch (SQLException e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleAjouterPanier() {
        Produit p = cbProduit.getValue();
        if (p == null || txtQuantite.getText().isEmpty() || txtPrixAchat.getText().isEmpty()) {
            showError("Error", "Product, quantity, and purchase price are required");
            return;
        }
        try {
            int qte = Integer.parseInt(txtQuantite.getText());
            BigDecimal prix = new BigDecimal(txtPrixAchat.getText());
            panier.add(new LigneCommandeFournisseur(p.getIdProduit(), p.getNom(), qte, prix));
            updateTotal();
            cbProduit.getSelectionModel().clearSelection();
            txtQuantite.clear();
            txtPrixAchat.clear();
        } catch (NumberFormatException e) {
            showError("Format Error", "Invalid numbers");
        }
    }

    @FXML
    private void handleValiderCommande() {
        Fournisseur f = cbFournisseur.getValue();
        if (f == null || panier.isEmpty()) {
            showError("Error", "Supplier and non-empty cart required");
            return;
        }
        try {
            commandesService.creerCommande(f.getIdFournisseur(), new ArrayList<>(panier), user.getIdUtilisateur());
            showSuccess("Order created!");
            panier.clear();
            updateTotal();
            cbFournisseur.getSelectionModel().clearSelection();
            loadHistory();
        } catch (Exception e) {
            showError("Order Error", e.getMessage());
        }
    }

    @FXML
    private void handleReceptionner() {
        CommandeFournisseur s = tableCommandes.getSelectionModel().getSelectedItem();
        if (s != null
                && confirm("Confirm Reception", "Receive order #" + s.getIdCommande() + "? Stock will be updated.")) {
            try {
                commandesService.recevoirCommande(s.getIdCommande(), user.getIdUtilisateur());
                showSuccess("Order received and stock updated!");
                loadHistory();
            } catch (Exception e) {
                showError("Reception Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        CommandeFournisseur s = tableCommandes.getSelectionModel().getSelectedItem();
        if (s != null && confirm("Cancel Order", "Cancel order #" + s.getIdCommande() + "?")) {
            try {
                commandesService.annulerCommande(s.getIdCommande(), user.getIdUtilisateur());
                showSuccess("Order cancelled.");
                loadHistory();
            } catch (SQLException e) {
                showError("Cancel Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimerLignePanier() {
        panier.remove(tablePanier.getSelectionModel().getSelectedItem());
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = panier.stream().map(LigneCommandeFournisseur::getTotaleLigne).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        lblTotalCommande.setText(String.format("%.2f TND", total));
        if (lblNbArticles != null) {
            lblNbArticles.setText(String.valueOf(panier.size()));
        }
    }

    @FXML
    private void handleVoirDetails() {
        CommandeFournisseur s = tableCommandes.getSelectionModel().getSelectedItem();
        if (s != null) {
            try {
                List<LigneCommandeFournisseur> lines = commandesService.getDetailsCommande(s.getIdCommande());
                StringBuilder sb = new StringBuilder("Order #" + s.getIdCommande() + "\nDetails:\n");
                for (LigneCommandeFournisseur l : lines) {
                    sb.append("- ").append(l.getNomProduit()).append(": ").append(l.getQuantiteCommandee())
                            .append(" x ").append(l.getPrixAchat()).append(" TND\n");
                }
                showInfo("Order Details", sb.toString());
            } catch (SQLException e) {
                showError("Error", e.getMessage());
            }
        }
    }
}
