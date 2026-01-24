package com.pharmacie.service;

import com.pharmacie.dao.ProduitDAO;
import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.Produit;
import com.pharmacie.model.LogActivite;
import com.pharmacie.exception.StockInsuffisantException;
import com.pharmacie.exception.ProduitIntrouvableException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Logique métier pour la gestion des stocks de produits.
 */
public class GestionStock {

    private final ProduitDAO produitDAO;
    private final LogActiviteDAO logDAO;

    public GestionStock() {
        this.produitDAO = new ProduitDAO();
        this.logDAO = new LogActiviteDAO();
    }

    public GestionStock(Connection connection) {
        this.produitDAO = new ProduitDAO(connection);
        this.logDAO = new LogActiviteDAO(connection);
    }

    public boolean ajouterProduit(Produit produit, int idUtilisateur) throws SQLException {
        if (produitDAO.ajouter(produit)) {
            logDAO.ajouter(
                    new LogActivite(LogActivite.TYPE_MAJ_STOCK, "Added product: " + produit.getNom(), idUtilisateur));
            return true;
        }
        return false;
    }

    public boolean modifierProduit(Produit produit, int idUtilisateur) throws SQLException {
        if (produitDAO.modifier(produit)) {
            logDAO.ajouter(new LogActivite(LogActivite.TYPE_MAJ_STOCK, "Modified product: " + produit.getNom(),
                    idUtilisateur));
            return true;
        }
        return false;
    }

    public boolean supprimerProduit(int idProduit, int idUtilisateur) throws SQLException, ProduitIntrouvableException {
        Produit produit = produitDAO.rechercherParId(idProduit);
        String name = produit.getNom();
        if (produitDAO.supprimer(idProduit)) {
            logDAO.ajouter(LogActivite.creerLogSuppression("Produit", name, idUtilisateur));
            return true;
        }
        return false;
    }

    /**
     * Vérifie si le stock est suffisant pour une commande.
     * 
     * @throws StockInsuffisantException si la quantité demandée dépasse le stock
     *                                   actuel.
     */
    public void verifierDisponibilite(int idProduit, int quantite)
            throws SQLException, ProduitIntrouvableException, StockInsuffisantException {
        Produit produit = produitDAO.rechercherParId(idProduit);
        if (!produit.estDisponible(quantite)) {
            throw new StockInsuffisantException(produit.getNom(), quantite, produit.getQuantiteStock());
        }
    }

    /**
     * Diminue le stock après une vente et enregistre l'opération dans les logs.
     */
    public void diminuerStock(int idProduit, int quantite, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException, StockInsuffisantException {
        verifierDisponibilite(idProduit, quantite);
        Produit produit = produitDAO.rechercherParId(idProduit);
        if (produitDAO.diminuerStock(idProduit, quantite)) {
            logDAO.ajouter(LogActivite.creerLogMajStock(produit.getNom(), -quantite, idUtilisateur));
        }
    }

    /**
     * Augmente le stock (retour client ou commande fournisseur) et log l'action.
     */
    public void augmenterStock(int idProduit, int quantite, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException {
        Produit produit = produitDAO.rechercherParId(idProduit);
        if (produitDAO.augmenterStock(idProduit, quantite)) {
            logDAO.ajouter(LogActivite.creerLogMajStock(produit.getNom(), quantite, idUtilisateur));
        }
    }

    public List<Produit> getProduitsEnAlerte() throws SQLException {
        return produitDAO.listerProduitsEnAlerte();
    }

    public Produit rechercherProduit(int idProduit) throws SQLException, ProduitIntrouvableException {
        return produitDAO.rechercherParId(idProduit);
    }

    public Produit rechercherProduitParCodeBarre(String codeBarre) throws SQLException, ProduitIntrouvableException {
        return produitDAO.rechercherParCodeBarre(codeBarre);
    }

    public List<Produit> rechercherProduitsParNom(String nom) throws SQLException {
        return produitDAO.rechercherParNom(nom);
    }

    public List<Produit> listerTousProduits() throws SQLException {
        return produitDAO.listerTous();
    }

    public String genererRapportStock() throws SQLException {
        List<Produit> produits = listerTousProduits();
        List<Produit> alertes = produitDAO.listerProduitsEnAlerte();

        StringBuilder rapport = new StringBuilder("=== STOCK REPORT ===\n\n")
                .append("Total products: ").append(produits.size()).append("\n")
                .append("Products in alert: ").append(alertes.size()).append("\n\n");

        if (!alertes.isEmpty()) {
            rapport.append("--- PRODUCTS IN ALERT ---\n");
            alertes.forEach(p -> rapport.append(String.format("- %s: %d units (Seuil: %d)\n",
                    p.getNom(), p.getQuantiteStock(), p.getSeuilAlerte())));
        }
        return rapport.toString();
    }

    /**
     * Calcule la valeur monétaire totale du stock actuel.
     */
    public double calculerValeurStock() throws SQLException {
        return produitDAO.listerTous().stream()
                .mapToDouble(p -> p.getPrixUnitaire().doubleValue() * p.getQuantiteStock())
                .sum();
    }
}
