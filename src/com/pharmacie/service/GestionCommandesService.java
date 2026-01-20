package com.pharmacie.service;

import com.pharmacie.dao.CommandeFournisseurDAO;
import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.dao.LigneCommandeFournisseurDAO;
import com.pharmacie.dao.ProduitDAO;
import com.pharmacie.model.CommandeFournisseur;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.model.LigneCommandeFournisseur;
import com.pharmacie.model.Produit;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GestionCommandesService {

    private CommandeFournisseurDAO commandeDAO;
    private LigneCommandeFournisseurDAO ligneCommandeDAO;
    private FournisseurDAO fournisseurDAO;
    private ProduitDAO produitDAO;

    public GestionCommandesService() throws ConnexionEchoueeException {
        // Constructeurs par défaut utilisant la connexion par défaut pour la lecture
        this.commandeDAO = new CommandeFournisseurDAO();
        this.ligneCommandeDAO = new LigneCommandeFournisseurDAO();
        this.fournisseurDAO = new FournisseurDAO();
        this.produitDAO = new ProduitDAO();
    }

    public List<Fournisseur> listerFournisseurs() throws SQLException {
        return fournisseurDAO.listerTous();
    }

    public List<Produit> listerProduits() throws SQLException {
        return produitDAO.listerTous();
    }

    public List<CommandeFournisseur> listerCommandes() throws SQLException {
        return commandeDAO.listerTous();
    }

    public List<CommandeFournisseur> listerCommandesParStatut(String statut) throws SQLException {
        return commandeDAO.listerParStatut(statut);
    }

    public List<LigneCommandeFournisseur> getDetailsCommande(int idCommande) throws SQLException {
        return ligneCommandeDAO.listerParCommande(idCommande);
    }

    /**
     * Crée une commande et ses lignes de manière transactionnelle.
     */
    public void creerCommande(CommandeFournisseur commande, List<LigneCommandeFournisseur> lignes)
            throws SQLException, ConnexionEchoueeException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            CommandeFournisseurDAO cDao = new CommandeFournisseurDAO(conn);
            LigneCommandeFournisseurDAO lDao = new LigneCommandeFournisseurDAO(conn);

            // 1. Créer la commande
            if (cDao.ajouter(commande)) {
                // 2. Créer les lignes
                for (LigneCommandeFournisseur ligne : lignes) {
                    ligne.setIdCommande(commande.getIdCommande());
                    lDao.ajouter(ligne);
                }

                conn.commit();
            } else {
                throw new SQLException("Échec de la création de la commande header.");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // NE PAS FERMER LA CONNEXION PARTAGÉE
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Valide la réception d'une commande et met à jour le stock.
     */
    public void receptionnerCommande(int idCommande) throws SQLException, ConnexionEchoueeException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            CommandeFournisseurDAO cDao = new CommandeFournisseurDAO(conn);
            LigneCommandeFournisseurDAO lDao = new LigneCommandeFournisseurDAO(conn);
            ProduitDAO pDao = new ProduitDAO(conn);

            CommandeFournisseur cmd = cDao.rechercherParId(idCommande);
            if (cmd != null && cmd.estEnCours()) {
                // 1. Mettre à jour le statut
                if (cDao.modifierStatut(idCommande, CommandeFournisseur.STATUT_RECUE, LocalDateTime.now())) {

                    // 2. Mettre à jour le stock pour chaque ligne
                    List<LigneCommandeFournisseur> lignes = lDao.listerParCommande(idCommande);
                    for (LigneCommandeFournisseur ligne : lignes) {
                        pDao.augmenterStock(ligne.getIdProduit(), ligne.getQuantiteCommandee());
                    }

                    conn.commit();
                } else {
                    throw new SQLException("Impossible de mettre à jour le statut de la commande.");
                }
            } else {
                throw new SQLException("Commande introuvable ou déjà traitée.");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // NE PAS FERMER LA CONNEXION PARTAGÉE
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void annulerCommande(int idCommande) throws SQLException, ConnexionEchoueeException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            CommandeFournisseurDAO cDao = new CommandeFournisseurDAO(conn);

            if (cDao.modifierStatut(idCommande, CommandeFournisseur.STATUT_ANNULEE, null)) {
                conn.commit();
            } else {
                throw new SQLException("Impossible d'annuler la commande.");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // NE PAS FERMER LA CONNEXION PARTAGÉE
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
