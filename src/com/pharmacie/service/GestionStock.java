package com.pharmacie.service;

import com.pharmacie.dao.ProduitDAO;
import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.Produit;
import com.pharmacie.model.LogActivite;
import com.pharmacie.exception.StockInsuffisantException;
import com.pharmacie.exception.ProduitIntrouvableException;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.SQLException;
import java.util.List;

/**
 * Service métier pour la gestion des stocks de produits.
 * Contient la logique métier et fait appel aux DAO.
 */
public class GestionStock {

    private ProduitDAO produitDAO;
    private LogActiviteDAO logDAO;

    /**
     * Constructeur par défaut.
     */
    public GestionStock() throws ConnexionEchoueeException {
        this.produitDAO = new ProduitDAO();
        this.logDAO = new LogActiviteDAO();
    }

    /**
     * Ajoute un nouveau produit au catalogue.
     */
    public boolean ajouterProduit(Produit produit, int idUtilisateur) throws SQLException {
        boolean success = produitDAO.ajouter(produit);

        if (success) {
            LogActivite log = new LogActivite(
                    LogActivite.TYPE_MAJ_STOCK,
                    "Ajout du produit: " + produit.getNom(),
                    idUtilisateur
            );
            logDAO.ajouter(log);
        }

        return success;
    }

    /**
     * Modifie un produit existant.
     */
    public boolean modifierProduit(Produit produit, int idUtilisateur) throws SQLException {
        boolean success = produitDAO.modifier(produit);

        if (success) {
            LogActivite log = new LogActivite(
                    LogActivite.TYPE_MAJ_STOCK,
                    "Modification du produit: " + produit.getNom(),
                    idUtilisateur
            );
            logDAO.ajouter(log);
        }

        return success;
    }

    /**
     * Supprime un produit du catalogue.
     */
    public boolean supprimerProduit(int idProduit, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException {

        Produit produit = produitDAO.rechercherParId(idProduit);
        String nomProduit = produit.getNom();

        boolean success = produitDAO.supprimer(idProduit);

        if (success) {
            LogActivite log = LogActivite.creerLogSuppression(
                    "Produit",
                    nomProduit,
                    idUtilisateur
            );
            logDAO.ajouter(log);
        }

        return success;
    }

    /**
     * Vérifie si un produit peut être vendu en quantité demandée.
     */
    public void verifierDisponibilite(int idProduit, int quantite)
            throws SQLException, ProduitIntrouvableException, StockInsuffisantException {

        Produit produit = produitDAO.rechercherParId(idProduit);

        if (!produit.estDisponible(quantite)) {
            throw new StockInsuffisantException(
                    produit.getNom(),
                    quantite,
                    produit.getQuantiteStock()
            );
        }
    }

    /**
     * Diminue le stock lors d'une vente.
     */
    public void diminuerStock(int idProduit, int quantite, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException, StockInsuffisantException {

        // Vérifier la disponibilité
        verifierDisponibilite(idProduit, quantite);

        Produit produit = produitDAO.rechercherParId(idProduit);

        // Diminuer le stock
        boolean success = produitDAO.diminuerStock(idProduit, quantite);

        if (success) {
            LogActivite log = LogActivite.creerLogMajStock(
                    produit.getNom(),
                    -quantite,
                    idUtilisateur
            );
            logDAO.ajouter(log);
        }
    }

    /**
     * Augmente le stock lors d'une réception de commande.
     */
    public void augmenterStock(int idProduit, int quantite, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException {

        Produit produit = produitDAO.rechercherParId(idProduit);

        boolean success = produitDAO.augmenterStock(idProduit, quantite);

        if (success) {
            LogActivite log = LogActivite.creerLogMajStock(
                    produit.getNom(),
                    quantite,
                    idUtilisateur
            );
            logDAO.ajouter(log);
        }
    }

    /**
     * Récupère les produits en alerte de stock.
     */
    public List<Produit> getProduitsEnAlerte() throws SQLException {
        return produitDAO.listerProduitsEnAlerte();
    }

    /**
     * Recherche un produit par ID.
     */
    public Produit rechercherProduit(int idProduit)
            throws SQLException, ProduitIntrouvableException {
        return produitDAO.rechercherParId(idProduit);
    }

    /**
     * Recherche un produit par code-barres.
     */
    public Produit rechercherProduitParCodeBarre(String codeBarre)
            throws SQLException, ProduitIntrouvableException {
        return produitDAO.rechercherParCodeBarre(codeBarre);
    }

    /**
     * Recherche des produits par nom.
     */
    public List<Produit> rechercherProduitsParNom(String nom) throws SQLException {
        return produitDAO.rechercherParNom(nom);
    }

    /**
     * Liste tous les produits.
     */
    public List<Produit> listerTousProduits() throws SQLException {
        return produitDAO.listerTous();
    }

    /**
     * Génère un rapport de l'état des stocks.
     */
    public String genererRapportStock() throws SQLException {
        List<Produit> produits = produitDAO.listerTous();
        List<Produit> alertes = produitDAO.listerProduitsEnAlerte();

        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DE STOCK ===\n\n");
        rapport.append("Nombre total de produits: ").append(produits.size()).append("\n");
        rapport.append("Produits en alerte: ").append(alertes.size()).append("\n\n");

        if (!alertes.isEmpty()) {
            rapport.append("--- PRODUITS EN ALERTE ---\n");
            for (Produit p : alertes) {
                rapport.append(String.format("- %s: %d unités (Seuil: %d)\n",
                        p.getNom(), p.getQuantiteStock(), p.getSeuilAlerte()));
            }
        }

        return rapport.toString();
    }

    /**
     * Calcule la valeur totale du stock.
     */
    public double calculerValeurStock() throws SQLException {
        List<Produit> produits = produitDAO.listerTous();
        double valeurTotale = 0.0;

        for (Produit p : produits) {
            valeurTotale += p.getPrixUnitaire().doubleValue() * p.getQuantiteStock();
        }

        return valeurTotale;
    }
}