package com.pharmacie.service;

import com.pharmacie.dao.*;
import com.pharmacie.model.*;
import com.pharmacie.exception.*;
import com.pharmacie.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GestionVente {

    private final VenteDAO venteDAO = new VenteDAO();
    private final LigneVenteDAO ligneVenteDAO = new LigneVenteDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final LogActiviteDAO logDAO = new LogActiviteDAO();
    private final GestionStock gestionStock = new GestionStock();

    /**
     * Crée une vente complète de manière atomique.
     * Gère la transaction, vérifie le stock, décrémente les quantités et log
     * l'action.
     */
    public Vente creerVente(Integer idClient, int idUtilisateur, List<LigneVente> lignes)
            throws SQLException, StockInsuffisantException, ProduitIntrouvableException, ConnexionEchoueeException {

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            VenteDAO transVenteDAO = new VenteDAO(conn);
            LigneVenteDAO transLigneVenteDAO = new LigneVenteDAO(conn);
            LogActiviteDAO transLogDAO = new LogActiviteDAO(conn);
            GestionStock transGestionStock = new GestionStock(conn);

            for (LigneVente ligne : lignes) {
                transGestionStock.verifierDisponibilite(ligne.getIdProduit(), ligne.getQuantiteVendue());
            }

            BigDecimal total = lignes.stream()
                    .map(LigneVente::calculerMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Vente vente = new Vente(total, idClient, idUtilisateur);
            if (!transVenteDAO.ajouter(vente))
                throw new SQLException("Failed to create sale");

            for (LigneVente ligne : lignes) {
                ligne.setIdVente(vente.getIdVente());
                transLigneVenteDAO.ajouter(ligne);
                transGestionStock.diminuerStock(ligne.getIdProduit(), ligne.getQuantiteVendue(), idUtilisateur);
            }

            transLogDAO.ajouter(LogActivite.creerLogVente(vente.getIdVente(), total.doubleValue(), idUtilisateur));
            conn.commit();
            return vente;

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(originalAutoCommit);
                conn.close();
            }
        }
    }

    public void ajouterLigneVente(int idVente, int idProduit, int quantite, int idUtilisateur)
            throws SQLException, StockInsuffisantException, ProduitIntrouvableException {

        gestionStock.verifierDisponibilite(idProduit, quantite);
        Produit produit = produitDAO.rechercherParId(idProduit);

        LigneVente ligne = new LigneVente(idVente, idProduit, quantite, produit.getPrixUnitaire());
        ligneVenteDAO.ajouter(ligne);

        Vente vente = venteDAO.rechercherParId(idVente);
        vente.ajouterMontant(ligne.calculerMontantTotal());
        venteDAO.modifier(vente);

        gestionStock.diminuerStock(idProduit, quantite, idUtilisateur);
    }

    public List<Vente> listerVentes() throws SQLException {
        return venteDAO.listerTous();
    }

    public List<LigneVente> getLignesVente(int idVente) throws SQLException {
        return ligneVenteDAO.listerParVente(idVente);
    }

    public double calculerChiffreAffaires() throws SQLException {
        return venteDAO.calculerChiffreAffairesTotal();
    }

    public List<String> getTopProduits(int limit) throws SQLException {
        return venteDAO.getTopProduits(limit);
    }

    public List<String> getCAJournalier(int jours) throws SQLException {
        return venteDAO.getCAJournalier(jours);
    }

    /**
     * Génère un rapport textuel des ventes pour une période donnée.
     */
    public String genererRapportVentes(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        List<Vente> ventes = venteDAO.listerParPeriode(debut, fin);
        double ca = venteDAO.calculerChiffreAffairesPeriode(debut, fin);

        StringBuilder rapport = new StringBuilder("=== SALES REPORT ===\n\n")
                .append(String.format("Period: %s to %s\n", debut, fin))
                .append(String.format("Total sales: %d\n", ventes.size()))
                .append(String.format("Turnover: %.2f TND\n\n", ca));

        if (!ventes.isEmpty()) {
            rapport.append("--- DETAILS ---\n");
            ventes.forEach(v -> rapport.append(String.format("Sale #%d - %s - %.2f TND\n",
                    v.getIdVente(), v.getDateVente(), v.getTotalVente())));
        }
        return rapport.toString();
    }

    /**
     * Supprime une vente et restaure les stocks correspondants.
     */
    public void supprimerVente(int idVente, int idUtilisateur) throws SQLException {
        try {
            List<LigneVente> lignes = ligneVenteDAO.listerParVente(idVente);
            for (LigneVente ligne : lignes) {
                gestionStock.augmenterStock(ligne.getIdProduit(), ligne.getQuantiteVendue(), idUtilisateur);
                ligneVenteDAO.supprimer(ligne.getIdLigneVente());
            }
            venteDAO.supprimer(idVente);
            logDAO.ajouter(new LogActivite("SUPPRESSION_VENTE", "Sale #" + idVente + " deleted", idUtilisateur));
        } catch (ProduitIntrouvableException e) {
            throw new SQLException("Error restoring stock", e);
        }
    }
}
