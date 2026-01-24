package com.pharmacie.service;

import com.pharmacie.dao.*;
import com.pharmacie.model.*;
import com.pharmacie.exception.*;
import com.pharmacie.utils.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing supplier orders.
 */
public class GestionCommande {

    private final CommandeFournisseurDAO commandeDAO = new CommandeFournisseurDAO();
    private final LigneCommandeFournisseurDAO ligneCommandeDAO = new LigneCommandeFournisseurDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final LogActiviteDAO logDAO = new LogActiviteDAO();

    public List<Fournisseur> listerFournisseurs() throws SQLException {
        return fournisseurDAO.listerTous();
    }

    public List<Produit> listerProduits() throws SQLException {
        return produitDAO.listerTous();
    }

    public CommandeFournisseur creerCommande(int idFournisseur, List<LigneCommandeFournisseur> lignes,
            int idUtilisateur) throws SQLException, ConnexionEchoueeException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            CommandeFournisseurDAO transCommandeDAO = new CommandeFournisseurDAO(conn);
            LigneCommandeFournisseurDAO transLigneCommandeDAO = new LigneCommandeFournisseurDAO(conn);
            LogActiviteDAO transLogDAO = new LogActiviteDAO(conn);

            CommandeFournisseur cmd = new CommandeFournisseur(CommandeFournisseur.STATUT_EN_COURS, idFournisseur);
            if (!transCommandeDAO.ajouter(cmd))
                throw new SQLException("Failed to create order");

            for (LigneCommandeFournisseur line : lignes) {
                line.setIdCommande(cmd.getIdCommande());
                transLigneCommandeDAO.ajouter(line);
            }

            transLogDAO.ajouter(new LogActivite(LogActivite.TYPE_MAJ_STOCK,
                    "Created supplier order #" + cmd.getIdCommande(), idUtilisateur));
            conn.commit();
            return cmd;
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

    public void recevoirCommande(int idCommande, int idUtilisateur)
            throws SQLException, ProduitIntrouvableException, ConnexionEchoueeException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            CommandeFournisseurDAO transCommandeDAO = new CommandeFournisseurDAO(conn);
            LigneCommandeFournisseurDAO transLigneCommandeDAO = new LigneCommandeFournisseurDAO(conn);
            GestionStock transGestionStock = new GestionStock(conn);
            LogActiviteDAO transLogDAO = new LogActiviteDAO(conn);

            CommandeFournisseur cmd = transCommandeDAO.rechercherParId(idCommande);
            if (cmd == null || !cmd.estEnCours())
                throw new SQLException("Order not found or not in progress: " + idCommande);

            List<LigneCommandeFournisseur> lines = transLigneCommandeDAO.listerParCommande(idCommande);
            for (LigneCommandeFournisseur line : lines) {
                transGestionStock.augmenterStock(line.getIdProduit(), line.getQuantiteCommandee(), idUtilisateur);
            }

            cmd.marquerCommeRecue();
            transCommandeDAO.modifier(cmd);
            transLogDAO.ajouter(new LogActivite(LogActivite.TYPE_MAJ_STOCK, "Received supplier order #" + idCommande,
                    idUtilisateur));
            conn.commit();
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

    public void annulerCommande(int idCommande, int idUtilisateur) throws SQLException {
        CommandeFournisseur cmd = commandeDAO.rechercherParId(idCommande);
        if (cmd == null || !cmd.estEnCours())
            throw new SQLException("Only orders in progress can be cancelled");

        cmd.annuler();
        commandeDAO.modifier(cmd);
        logDAO.ajouter(new LogActivite(LogActivite.TYPE_SUPPRESSION, "Cancelled supplier order #" + idCommande,
                idUtilisateur));
    }

    public List<CommandeFournisseur> listerCommandes() throws SQLException {
        return commandeDAO.listerTous();
    }

    public List<LigneCommandeFournisseur> getDetailsCommande(int idCommande) throws SQLException {
        return ligneCommandeDAO.listerParCommande(idCommande);
    }
}
