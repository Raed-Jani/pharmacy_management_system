package com.pharmacie.ui.controller;

import com.pharmacie.model.*;
import com.pharmacie.service.GestionVente;
import com.pharmacie.dao.ClientDAO;
import com.pharmacie.dao.ProduitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionVentesController extends BaseController {

    @FXML
    private Label lblDate;
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
    private Label lblNbArticles;
    @FXML
    private Button btnValiderVente;

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

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final GestionVente gestionVente = new GestionVente();

    private final ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private final ObservableList<LigneVente> panierList = FXCollections.observableArrayList();
    private final ObservableList<Vente> historiqueVentesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        loadInitialData();
        setupSearch();

        spinnerQuantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        if (lblDate != null) {
            lblDate.setText(
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void setupTables() {
        colNomProduit.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrixProduit.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStockProduit.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        colPanierProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));
        colPanierPrix.setCellValueFactory(new PropertyValueFactory<>("prixApplique"));
        colPanierTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMontantTotalFormate()));
        tablePanier.setItems(panierList);

        setupHistoryTable();
    }

    private void setupHistoryTable() {
        colHistoId.setCellValueFactory(new PropertyValueFactory<>("idVente"));
        colHistoDate.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        colHistoTotal.setCellValueFactory(new PropertyValueFactory<>("totalVente"));
        colHistoUtilisateur.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getLoginUtilisateur() != null ? d.getValue().getLoginUtilisateur() : "N/A"));
        colHistoClient.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNomClient() != null ? d.getValue().getNomClient() : "Guest"));

        colHistoDate.setCellFactory(c -> new TableCell<Vente, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime it, boolean em) {
                super.updateItem(it, em);
                setText(em || it == null ? null
                        : it.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
        });
        tableHistoriqueVentes.setItems(historiqueVentesList);
        tableHistoriqueVentes.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean sel = newVal != null;
            if (btnVoirDetails != null)
                btnVoirDetails.setDisable(!sel);
            if (btnSupprimerVente != null)
                btnSupprimerVente.setDisable(!sel);
        });
    }

    private void setupSearch() {
        FilteredList<Produit> filtered = new FilteredList<>(produitsList, p -> true);
        tableProduits.setItems(filtered);
        fieldRechercheProduit.textProperty().addListener((obs, old, val) -> {
            filtered.setPredicate(
                    p -> val == null || val.isEmpty() || p.getNom().toLowerCase().contains(val.toLowerCase())
                            || (p.getCodeBarre() != null && p.getCodeBarre().contains(val)));
        });
    }

    private void loadInitialData() {
        try {
            List<Client> clients = clientDAO.listerTous();
            comboClient.setItems(FXCollections.observableArrayList(clients));
            StringConverter<Client> converter = new StringConverter<>() {
                @Override
                public String toString(Client c) {
                    return c == null ? "" : c.getNom() + " " + c.getPrenom();
                }

                @Override
                public Client fromString(String s) {
                    return null;
                }
            };
            comboClient.setConverter(converter);
            if (comboClientRecherche != null)
                comboClientRecherche.setConverter(converter);

            produitsList.setAll(produitDAO.listerTous());
            loadHistory();
        } catch (Exception e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void loadHistory() {
        try {
            historiqueVentesList.setAll(gestionVente.listerVentes());
        } catch (Exception e) {
            showError("History Error", e.getMessage());
        }
    }

    @FXML
    private void actionAjouterPanier() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Selection", "Please select a product.");
            return;
        }

        int qty = spinnerQuantite.getValue();
        if (selected.getQuantiteStock() < qty) {
            showError("Stock Error", "Available stock: " + selected.getQuantiteStock());
            return;
        }

        for (LigneVente ligne : panierList) {
            if (ligne.getIdProduit() == selected.getIdProduit()) {
                if (ligne.getQuantiteVendue() + qty > selected.getQuantiteStock()) {
                    showError("Stock Error", "Total cart quantity exceeds stock.");
                    return;
                }
                ligne.setQuantiteVendue(ligne.getQuantiteVendue() + qty);
                tablePanier.refresh();
                updateTotal();
                return;
            }
        }

        LigneVente ligne = new LigneVente();
        ligne.setIdProduit(selected.getIdProduit());
        ligne.setNomProduit(selected.getNom());
        ligne.setPrixApplique(selected.getPrixUnitaire());
        ligne.setQuantiteVendue(qty);
        panierList.add(ligne);
        updateTotal();
    }

    @FXML
    private void actionRetirerPanier() {
        panierList.remove(tablePanier.getSelectionModel().getSelectedItem());
        updateTotal();
    }

    @FXML
    private void actionViderPanier() {
        panierList.clear();
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = panierList.stream().map(LigneVente::calculerMontantTotal).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        labelTotal.setText(String.format("%.2f TND", total));
        if (lblNbArticles != null) {
            lblNbArticles.setText(String.valueOf(panierList.size()));
        }
        btnValiderVente.setDisable(panierList.isEmpty());
    }

    @FXML
    private void handleNouveauClient() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("New Client");
        ButtonType btnAdd = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        TextField txtNom = new TextField();
        TextField txtPrenom = new TextField();
        TextField txtTel = new TextField();
        grid.add(new Label("Last Name:"), 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(txtPrenom, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(txtTel, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == btnAdd) {
                Client c = new Client();
                c.setNom(txtNom.getText());
                c.setPrenom(txtPrenom.getText());
                c.setTelephone(txtTel.getText());
                try {
                    if (clientDAO.ajouter(c))
                        return c;
                } catch (Exception e) {
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            loadInitialData();
            comboClient.setValue(c);
        });
    }

    @FXML
    private void actionValiderVente() {
        if (panierList.isEmpty())
            return;
        try {
            if (user == null) {
                showError("Error", "No user logged in.");
                return;
            }
            Client c = comboClient.getValue();
            Vente v = gestionVente.creerVente(c != null ? c.getIdClient() : null, user.getIdUtilisateur(),
                    new ArrayList<>(panierList));
            showSuccess("Sale validated! Total: " + v.getTotalVente() + " TND");
            panierList.clear();
            updateTotal();
            loadInitialData();
        } catch (Exception e) {
            showError("Sale Error", e.getMessage());
        }
    }

    @FXML
    private void handleRechercherVentes() {
        try {
            loadHistory(); // Simple approach: reload all and filter in memory
            java.time.LocalDate start = dateDebut.getValue();
            java.time.LocalDate end = dateFin.getValue();
            Client client = comboClientRecherche.getValue();

            historiqueVentesList.removeIf(v -> (start != null && v.getDateVente().toLocalDate().isBefore(start)) ||
                    (end != null && v.getDateVente().toLocalDate().isAfter(end)) ||
                    (client != null && v.getIdClient() != null && !v.getIdClient().equals(client.getIdClient())));
        } catch (Exception e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleResetRecherche() {
        dateDebut.setValue(null);
        dateFin.setValue(null);
        comboClientRecherche.setValue(null);
        loadHistory();
    }

    @FXML
    private void handleActualiserHistorique() {
        loadHistory();
    }

    @FXML
    private void handleRafraichir() {
        loadInitialData();
    }

    @FXML
    private void handleVoirDetails() {
        Vente s = tableHistoriqueVentes.getSelectionModel().getSelectedItem();
        if (s != null) {
            try {
                List<LigneVente> lines = gestionVente.getLignesVente(s.getIdVente());
                StringBuilder sb = new StringBuilder("Sale #" + s.getIdVente() + "\nDetails:\n");
                for (LigneVente l : lines) {
                    sb.append("- ").append(l.getNomProduit()).append(": ").append(l.getQuantiteVendue()).append(" x ")
                            .append(l.getPrixApplique()).append(" TND\n");
                }
                sb.append("Total: ").append(s.getTotalVente()).append(" TND");
                showInfo("Sale Details", sb.toString());
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimerVente() {
        Vente s = tableHistoriqueVentes.getSelectionModel().getSelectedItem();
        if (s != null && confirm("Delete Sale", "Are you sure? This is an experimental feature.")) {
            showInfo("Info",
                    "Sale deletion requires additional backend logic for stock reconciliation. Feature currently disabled.");
        }
    }
}
