package com.pharmacie.dao;

import com.pharmacie.model.LigneCommande;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les lignes de commande fournisseur.
 */
public class LigneCommandeDAO {

    private DBConnection dbConnection;
    private Connection manualConnection;

    /**
     * Constructeur avec connexion spécifique (pour compatibilité).
     */
    public LigneCommandeDAO(Connection connection) {
        this.manualConnection = connection;
    }

    /**
     * Constructeur utilisant le singleton DBConnection.
     */
    public LigneCommandeDAO() throws ConnexionEchoueeException {
        this.dbConnection = DBConnection.getInstance();
    }

    private Connection getConnection() throws SQLException {
        if (manualConnection != null) {
            return manualConnection;
        }
        try {
            return dbConnection.getAdminConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion à la base de données", e);
        }
    }

    /**
     * Ajoute une ligne de commande.
     */
    public boolean ajouter(LigneCommande ligne) throws SQLException {
        String sql = "INSERT INTO LigneCommande (id_commande, id_produit, quantite_commandee, prix_achat) " +
                "VALUES (?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ligne.getIdCommande());
            stmt.setInt(2, ligne.getIdProduit());
            stmt.setInt(3, ligne.getQuantiteCommandee());
            stmt.setBigDecimal(4, ligne.getPrixAchat());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ligne.setIdLigneCmd(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Récupère toutes les lignes d'une commande.
     */
    public List<LigneCommande> listerParCommande(int idCommande) throws SQLException {
        String sql = "SELECT lc.*, p.nom " +
                "FROM LigneCommande lc " +
                "JOIN Produit p ON lc.id_produit = p.id_produit " +
                "WHERE lc.id_commande = ?";
        List<LigneCommande> lignes = new ArrayList<>();

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneCommande(rs));
                }
            }
        }
        return lignes;
    }

    /**
     * Supprime une ligne de commande.
     */
    public boolean supprimer(int idLigneCmd) throws SQLException {
        String sql = "DELETE FROM LigneCommande WHERE id_ligne_cmd = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLigneCmd);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet LigneCommande.
     */
    private LigneCommande mapResultSetToLigneCommande(ResultSet rs) throws SQLException {
        LigneCommande ligne = new LigneCommande(
                rs.getInt("id_ligne_cmd"),
                rs.getInt("id_commande"),
                rs.getInt("id_produit"),
                rs.getInt("quantite_commandee"),
                rs.getBigDecimal("prix_achat"));

        String nomProduit = rs.getString("nom");
        if (nomProduit != null) {
            ligne.setNomProduit(nomProduit);
        }

        return ligne;
    }
}
