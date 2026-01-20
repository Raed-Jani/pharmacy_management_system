package com.pharmacie.service;

import com.pharmacie.dao.*;
import com.pharmacie.model.*;
import com.pharmacie.exception.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service métier pour la gestion des ventes.
 * Gère les transactions de vente complètes.
 */
public class GestionVente {

    private VenteDAO venteDAO;
    private LigneVenteDAO ligneVenteDAO;
    private ProduitDAO produitDAO;
    private LogActiviteDAO logDAO;
    private GestionStock gestionStock;

    public GestionVente() throws ConnexionEchoueeException {
        this.venteDAO = new VenteDAO();
        this.ligneVenteDAO = new LigneVenteDAO();
        this.produitDAO = new ProduitDAO();
        this.logDAO = new LogActiviteDAO();
        this.gestionStock = new GestionStock();
    }

    /**
     * Crée une nouvelle vente complète avec ses lignes.
     * Transaction atomique : soit tout réussit, soit tout échoue.
     */
    public Vente creerVente(Integer idClient, int idUtilisateur, List<LigneVente> lignes)
            throws SQLException, StockInsuffisantException, ProduitIntrouvableException {

        // 1. Vérifier la disponibilité de tous les produits
        for (LigneVente ligne : lignes) {
            gestionStock.verifierDisponibilite(ligne.getIdProduit(), ligne.getQuantiteVendue());
        }

        // 2. Calculer le total de la vente
        BigDecimal total = BigDecimal.ZERO;
        for (LigneVente ligne : lignes) {
            total = total.add(ligne.calculerMontantTotal());
        }

        // 3. Créer la vente
        Vente vente = new Vente(total, idClient, idUtilisateur);
        boolean venteCreee = venteDAO.ajouter(vente);

        if (!venteCreee) {
            throw new SQLException("Échec de création de la vente");
        }

        // 4. Ajouter les lignes de vente et mettre à jour les stocks
        for (LigneVente ligne : lignes) {
            ligne.setIdVente(vente.getIdVente());
            ligneVenteDAO.ajouter(ligne);

            // Diminuer le stock
            gestionStock.diminuerStock(ligne.getIdProduit(), ligne.getQuantiteVendue(), idUtilisateur);
        }

        // 5. Créer un log
        LogActivite log = LogActivite.creerLogVente(
                vente.getIdVente(),
                total.doubleValue(),
                idUtilisateur);
        logDAO.ajouter(log);

        return vente;
    }

    /**
     * Ajoute une ligne à une vente existante.
     */
    public void ajouterLigneVente(int idVente, int idProduit, int quantite, int idUtilisateur)
            throws SQLException, StockInsuffisantException, ProduitIntrouvableException {

        // Vérifier la disponibilité
        gestionStock.verifierDisponibilite(idProduit, quantite);

        // Récupérer le produit pour le prix
        Produit produit = produitDAO.rechercherParId(idProduit);

        // Créer la ligne de vente
        LigneVente ligne = new LigneVente(
                idVente,
                idProduit,
                quantite,
                produit.getPrixUnitaire());

        ligneVenteDAO.ajouter(ligne);

        // Mettre à jour le total de la vente
        Vente vente = venteDAO.rechercherParId(idVente);
        vente.ajouterMontant(ligne.calculerMontantTotal());
        venteDAO.modifier(vente);

        // Diminuer le stock
        gestionStock.diminuerStock(idProduit, quantite, idUtilisateur);
    }

    /**
     * Récupère une vente avec toutes ses lignes.
     */
    public Vente getVenteComplete(int idVente) throws SQLException {
        Vente vente = venteDAO.rechercherParId(idVente);
        if (vente != null) {
            List<LigneVente> lignes = ligneVenteDAO.listerParVente(idVente);
            // Vous pouvez stocker les lignes dans la vente si vous ajoutez un attribut
        }
        return vente;
    }

    /**
     * Récupère les lignes d'une vente.
     */
    public List<LigneVente> getLignesVente(int idVente) throws SQLException {
        return ligneVenteDAO.listerParVente(idVente);
    }

    /**
     * Liste toutes les ventes.
     */
    public List<Vente> listerVentes() throws SQLException {
        return venteDAO.listerTous();
    }

    /**
     * Récupère l'historique d'achats d'un client.
     */
    public List<Vente> getHistoriqueClient(int idClient) throws SQLException {
        return venteDAO.listerParClient(idClient);
    }

    /**
     * Calcule le chiffre d'affaires total.
     */
    public double calculerChiffreAffaires() throws SQLException {
        return venteDAO.calculerChiffreAffairesTotal();
    }

    /**
     * Calcule le CA sur une période.
     */
    public double calculerChiffreAffairesPeriode(LocalDateTime debut, LocalDateTime fin)
            throws SQLException {
        return venteDAO.calculerChiffreAffairesPeriode(debut, fin);
    }

    /**
     * Génère un rapport des ventes.
     */
    public String genererRapportVentes(LocalDateTime debut, LocalDateTime fin)
            throws SQLException {

        List<Vente> ventes = venteDAO.listerParPeriode(debut, fin);
        double ca = venteDAO.calculerChiffreAffairesPeriode(debut, fin);

        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DES VENTES ===\n\n");
        rapport.append(String.format("Période: %s à %s\n", debut, fin));
        rapport.append(String.format("Nombre de ventes: %d\n", ventes.size()));
        rapport.append(String.format("Chiffre d'affaires: %.2f TND\n\n", ca));

        if (!ventes.isEmpty()) {
            rapport.append("--- DÉTAIL DES VENTES ---\n");
            for (Vente v : ventes) {
                rapport.append(String.format("Vente #%d - %s - %.2f TND\n",
                        v.getIdVente(),
                        v.getDateVente(),
                        v.getTotalVente()));
            }
        }

        return rapport.toString();
    }

    /**
     * Récupère le top des produits.
     */
    public List<String> getTopProduits(int limit) throws SQLException {
        return venteDAO.getTopProduits(limit);
    }

    /**
     * Récupère le CA journalier.
     */
    public List<String> getCAJournalier(int jours) throws SQLException {
        return venteDAO.getCAJournalier(jours);
    }

    /**
     * Supprime une vente et restaure les stocks.
     */
    public void supprimerVente(int idVente, int idUtilisateur) throws SQLException {
        try {
            List<LigneVente> lignes = ligneVenteDAO.listerParVente(idVente);

            // 1. Restaurer les stocks
            for (LigneVente ligne : lignes) {
                gestionStock.augmenterStock(ligne.getIdProduit(), ligne.getQuantiteVendue(), idUtilisateur);
            }

            // 2. Supprimer les lignes de vente
            for (LigneVente ligne : lignes) {
                ligneVenteDAO.supprimer(ligne.getIdLigneVente());
            }

            // 3. Supprimer la vente
            venteDAO.supprimer(idVente);

            // 4. Logger l'action
            LogActivite log = new LogActivite("SUPPRESSION_VENTE", "Vente #" + idVente + " supprimée.", idUtilisateur);
            logDAO.ajouter(log);

        } catch (ProduitIntrouvableException e) {
            throw new SQLException("Erreur lors de la restauration du stock: " + e.getMessage());
        }
    }
}
