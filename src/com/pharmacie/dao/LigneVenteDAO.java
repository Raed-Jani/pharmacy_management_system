package com.pharmacie.dao;

import com.pharmacie.model.LigneVente;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneVenteDAO {

    private Connection manualConnection;

    public LigneVenteDAO() {
    }

    public LigneVenteDAO(Connection connection) {
        this.manualConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (manualConnection != null)
            return manualConnection;
        try {
            return DBConnection.getConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Connection error: " + e.getMessage(), e);
        }
    }

    public boolean ajouter(LigneVente lv) throws SQLException {
        String query = "INSERT INTO LigneVente (id_vente, id_produit, quantite_vendue, prix_applique) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, lv.getIdVente());
            stmt.setInt(2, lv.getIdProduit());
            stmt.setInt(3, lv.getQuantiteVendue());
            stmt.setBigDecimal(4, lv.getPrixApplique());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        lv.setIdLigneVente(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public LigneVente rechercherParId(int idLigneVente) throws SQLException {
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit WHERE lv.id_ligne_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idLigneVente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    public List<LigneVente> listerTous() throws SQLException {
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit ORDER BY lv.id_ligne_vente DESC";
        return fetchList(query, null);
    }

    public List<LigneVente> listerParVente(int idVente) throws SQLException {
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit " +
                "WHERE lv.id_vente = ? ORDER BY lv.id_ligne_vente";
        return fetchList(query, idVente);
    }

    public List<LigneVente> listerParProduit(int idProduit) throws SQLException {
        String query = "SELECT lv.*, p.nom, p.prix_unitaire FROM LigneVente lv " +
                "LEFT JOIN Produit p ON lv.id_produit = p.id_produit " +
                "WHERE lv.id_produit = ? ORDER BY lv.id_ligne_vente DESC";
        return fetchList(query, idProduit);
    }

    private List<LigneVente> fetchList(String query, Integer id) throws SQLException {
        List<LigneVente> lines = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            if (id != null)
                stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    lines.add(mapperResultSet(rs));
            }
        }
        return lines;
    }

    public boolean modifier(LigneVente lv) throws SQLException {
        String query = "UPDATE LigneVente SET id_vente = ?, id_produit = ?, quantite_vendue = ?, prix_applique = ? WHERE id_ligne_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, lv.getIdVente());
            stmt.setInt(2, lv.getIdProduit());
            stmt.setInt(3, lv.getQuantiteVendue());
            stmt.setBigDecimal(4, lv.getPrixApplique());
            stmt.setInt(5, lv.getIdLigneVente());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idLigneVente) throws SQLException {
        String query = "DELETE FROM LigneVente WHERE id_ligne_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idLigneVente);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimerParVente(int idVente) throws SQLException {
        String query = "DELETE FROM LigneVente WHERE id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idVente);
            return stmt.executeUpdate() > 0;
        }
    }

    public BigDecimal calculerTotalVente(int idVente) throws SQLException {
        String query = "SELECT COALESCE(SUM(quantite_vendue * prix_applique), 0) as total FROM LigneVente WHERE id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        }
    }

    public int compterLignesVente(int idVente) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM LigneVente WHERE id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public int compterQuantiteVendue(int idProduit) throws SQLException {
        String query = "SELECT COALESCE(SUM(quantite_vendue), 0) as total FROM LigneVente WHERE id_produit = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

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
