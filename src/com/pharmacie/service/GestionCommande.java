package com.pharmacie.service;

import com.pharmacie.dao.*;
import com.pharmacie.model.*;
import com.pharmacie.exception.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Service métier pour la gestion des commandes fournisseurs.
 * Gère les commandes complètes et la mise à jour des stocks.
 */
public class GestionCommande {

    private CommandeFournisseurDAO commandeDAO;
    private LigneCommandeDAO ligneCommandeDAO;
    private ProduitDAO produitDAO;
    private LogActiviteDAO logDAO;
    private GestionStock gestionStock;

    public GestionCommande() throws ConnexionEchoueeException {
        this.commandeDAO = new CommandeFournisseurDAO();
        this.ligneCommandeDAO = new LigneCommandeDAO();
        this.produitDAO = new ProduitDAO();
        this.logDAO = new LogActiviteDAO();
        this.gestionStock = new GestionStock();
    }

    /**
     * Crée une nouvelle commande fournisseur avec ses lignes.
     */
    public CommandeFournisseur creerCommande(int idFournisseur, List<LigneCommande> lignes,
                                             int idUtilisateur) throws SQLException {

        // 1. Créer la commande
        CommandeFournisseur commande = new CommandeFournisseur(
                CommandeFournisseur.STATUT_EN_COURS,
                idFournisseur
        );

        boolean commandeCreee = commandeDAO.ajouter(commande);

        if (!commandeCreee) {
            throw new SQLException("Échec de création de la commande");
        }

        // 2. Ajouter les lignes de commande
        for (LigneCommande ligne : lignes) {
            ligne.setIdCommande(commande.getIdCommande());
            ligneCommandeDAO.ajouter(ligne);
        }

        // 3. Créer un log
        LogActivite log = new LogActivite(
                LogActivite.TYPE_MAJ_STOCK,
                "Création de la commande fournisseur #" + commande.getIdCommande(),
                idUtilisateur
        );
        logDAO.ajouter(log);

        return commande;
    }

    /**
     * Réceptionne une commande et met à jour les stocks.
     */
    public void recevoirCommande(int idCommande, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException {

        // 1. Récupérer la commande
        CommandeFournisseur commande = commandeDAO.rechercherParId(idCommande);

        if (commande == null) {
            throw new SQLException("Commande introuvable: " + idCommande);
        }

        if (!commande.estEnCours()) {
            throw new SQLException("La commande n'est pas en cours");
        }

        // 2. Récupérer les lignes de commande
        List<LigneCommande> lignes = ligneCommandeDAO.listerParCommande(idCommande);

        // 3. Mettre à jour les stocks
        for (LigneCommande ligne : lignes) {
            gestionStock.augmenterStock(
                    ligne.getIdProduit(),
                    ligne.getQuantiteCommandee(),
                    idUtilisateur
            );
        }

        // 4. Marquer la commande comme reçue
        commande.marquerCommeRecue();
        commandeDAO.modifier(commande);

        // 5. Créer un log
        LogActivite log = new LogActivite(
                LogActivite.TYPE_MAJ_STOCK,
                "Réception de la commande fournisseur #" + idCommande,
                idUtilisateur
        );
        logDAO.ajouter(log);
    }

    /**
     * Annule une commande.
     */
    public void annulerCommande(int idCommande, int idUtilisateur) throws SQLException {
        CommandeFournisseur commande = commandeDAO.rechercherParId(idCommande);

        if (commande == null) {
            throw new SQLException("Commande introuvable: " + idCommande);
        }

        if (!commande.estEnCours()) {
            throw new SQLException("Seules les commandes en cours peuvent être annulées");
        }

        commande.annuler();
        commandeDAO.modifier(commande);

        LogActivite log = new LogActivite(
                LogActivite.TYPE_SUPPRESSION,
                "Annulation de la commande fournisseur #" + idCommande,
                idUtilisateur
        );
        logDAO.ajouter(log);
    }

    /**
     * Liste toutes les commandes.
     */
    public List<CommandeFournisseur> listerCommandes() throws SQLException {
        return commandeDAO.listerTous();
    }

    /**
     * Liste les commandes par statut.
     */
    public List<CommandeFournisseur> listerCommandesParStatut(String statut) throws SQLException {
        return commandeDAO.listerParStatut(statut);
    }

    /**
     * Liste les commandes d'un fournisseur.
     */
    public List<CommandeFournisseur> listerCommandesParFournisseur(int idFournisseur)
            throws SQLException {
        return commandeDAO.listerParFournisseur(idFournisseur);
    }

    /**
     * Récupère les lignes d'une commande.
     */
    public List<LigneCommande> getLignesCommande(int idCommande) throws SQLException {
        return ligneCommandeDAO.listerParCommande(idCommande);
    }

    /**
     * Génère un rapport des commandes en cours.
     */
    public String genererRapportCommandesEnCours() throws SQLException {
        List<CommandeFournisseur> commandes = commandeDAO.listerParStatut(
                CommandeFournisseur.STATUT_EN_COURS
        );

        StringBuilder rapport = new StringBuilder();
        rapport.append("=== COMMANDES FOURNISSEURS EN COURS ===\n\n");
        rapport.append("Nombre de commandes: ").append(commandes.size()).append("\n\n");

        if (!commandes.isEmpty()) {
            rapport.append("--- DÉTAIL ---\n");
            for (CommandeFournisseur c : commandes) {
                rapport.append(String.format("Commande #%d - Fournisseur: %s - Date: %s\n",
                        c.getIdCommande(),
                        c.getNomFournisseur(),
                        c.getDateCreation()));
            }
        }

        return rapport.toString();
    }
}
