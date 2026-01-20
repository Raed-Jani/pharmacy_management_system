package com.pharmacie.dao;

import com.pharmacie.model.LigneVente;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les lignes de vente
 */
public class LigneVenteDAO {

    private DBConnection dbConnection;

    public LigneVenteDAO() throws ConnexionEchoueeException {
        this.dbConnection = DBConnection.getInstance();
    }

    private Connection getConnection() throws SQLException {
        try {
            return dbConnection.getAdminConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Ajoute une nouvelle ligne de vente
     */
    public boolean ajouter(LigneVente ligneVente) throws SQLException {
        String query = "INSERT INTO LigneVente (id_vente, id_produit, quantite_vendue, prix_applique) VALUES (?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ligneVente.getIdVente());
            stmt.setInt(2, ligneVente.getIdProduit());
            stmt.setInt(3, ligneVente.getQuantiteVendue());
            stmt.setBigDecimal(4, ligneVente.getPrixApplique());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        ligneVente.setIdLigneVente(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Récupère une ligne de vente par ID
     */
    public LigneVente rechercherParId(int idLigneVente) throws SQLException {
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p. id_produit " +
                "WHERE lv.id_ligne_vente = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idLigneVente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Liste toutes les lignes de vente
     */
    public List<LigneVente> listerTous() throws SQLException {
        List<LigneVente> lignes = new ArrayList<>();
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit " +
                "ORDER BY lv.id_ligne_vente DESC";

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                lignes.add(mapperResultSet(rs));
            }
        }
        return lignes;
    }

    /**
     * Liste les lignes d'une vente
     */
    public List<LigneVente> listerParVente(int idVente) throws SQLException {
        List<LigneVente> lignes = new ArrayList<>();
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit " +
                "WHERE lv.id_vente = ?  ORDER BY lv.id_ligne_vente";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapperResultSet(rs));
                }
            }
        }
        return lignes;
    }

    /**
     * Liste les lignes d'un produit
     */
    public List<LigneVente> listerParProduit(int idProduit) throws SQLException {
        List<LigneVente> lignes = new ArrayList<>();
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit " +
                "WHERE lv.id_produit = ? ORDER BY lv.id_ligne_vente DESC";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapperResultSet(rs));
                }
            }
        }
        return lignes;
    }

    /**
     * Modifie une ligne de vente
     */
    public boolean modifier(LigneVente ligneVente) throws SQLException {
        String query = "UPDATE LigneVente SET id_vente = ?, id_produit = ?, quantite_vendue = ?, prix_applique = ? " +
                "WHERE id_ligne_vente = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ligneVente.getIdVente());
            stmt.setInt(2, ligneVente.getIdProduit());
            stmt.setInt(3, ligneVente.getQuantiteVendue());
            stmt.setBigDecimal(4, ligneVente.getPrixApplique());
            stmt.setInt(5, ligneVente.getIdLigneVente());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Supprime une ligne de vente
     */
    public boolean supprimer(int idLigneVente) throws SQLException {
        String query = "DELETE FROM LigneVente WHERE id_ligne_vente = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idLigneVente);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Supprime toutes les lignes d'une vente
     */
    public boolean supprimerParVente(int idVente) throws SQLException {
        String query = "DELETE FROM LigneVente WHERE id_vente = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idVente);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Calcule le total d'une vente
     */
    public BigDecimal calculerTotalVente(int idVente) throws SQLException {
        String query = "SELECT COALESCE(SUM(quantite_vendue * prix_applique), 0) as total FROM LigneVente WHERE id_vente = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Compte le nombre de lignes d'une vente
     */
    public int compterLignesVente(int idVente) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM LigneVente WHERE id_vente = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        }
    }

    /**
     * Compte la quantité totale vendue d'un produit
     */
    public int compterQuantiteVendue(int idProduit) throws SQLException {
        String query = "SELECT COALESCE(SUM(quantite_vendue), 0) as total FROM LigneVente WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        }
    }

    /**
     * Mappe un ResultSet vers une entité LigneVente
     */
    private LigneVente mapperResultSet(ResultSet rs) throws SQLException {
        LigneVente lv = new LigneVente();
        lv.setIdLigneVente(rs.getInt("id_ligne_vente"));
        lv.setIdVente(rs.getInt("id_vente"));
        lv.setIdProduit(rs.getInt("id_produit"));
        lv.setQuantiteVendue(rs.getInt("quantite_vendue"));
        lv.setPrixApplique(rs.getBigDecimal("prix_applique"));
        lv.setNomProduit(rs.getString("nom"));
        return lv;
    }
}