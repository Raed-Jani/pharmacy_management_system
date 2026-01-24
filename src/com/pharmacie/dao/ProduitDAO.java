package com.pharmacie.dao;

import com.pharmacie.model.Produit;
import com.pharmacie.exception.ProduitIntrouvableException;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    private Connection manualConnection;

    public ProduitDAO(Connection connection) {
        this.manualConnection = connection;
    }

    public ProduitDAO() {
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

    public boolean ajouter(Produit p) throws SQLException {
        String sql = "INSERT INTO Produit (nom, description, code_barre, prix_unitaire, quantite_stock, seuil_alerte, date_expiration, id_fournisseur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNom());
            stmt.setString(2, p.getDescription());
            stmt.setString(3, p.getCodeBarre());
            stmt.setBigDecimal(4, p.getPrixUnitaire());
            stmt.setInt(5, p.getQuantiteStock());
            stmt.setInt(6, p.getSeuilAlerte());
            stmt.setDate(7, p.getDateExpiration() != null ? Date.valueOf(p.getDateExpiration()) : null);
            if (p.getIdFournisseur() != null)
                stmt.setInt(8, p.getIdFournisseur());
            else
                stmt.setNull(8, Types.INTEGER);

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        p.setIdProduit(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public boolean modifier(Produit p) throws SQLException {
        String sql = "UPDATE Produit SET nom=?, description=?, code_barre=?, prix_unitaire=?, quantite_stock=?, seuil_alerte=?, date_expiration=?, id_fournisseur=? WHERE id_produit=?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, p.getNom());
            stmt.setString(2, p.getDescription());
            stmt.setString(3, p.getCodeBarre());
            stmt.setBigDecimal(4, p.getPrixUnitaire());
            stmt.setInt(5, p.getQuantiteStock());
            stmt.setInt(6, p.getSeuilAlerte());
            stmt.setDate(7, p.getDateExpiration() != null ? Date.valueOf(p.getDateExpiration()) : null);
            if (p.getIdFournisseur() != null)
                stmt.setInt(8, p.getIdFournisseur());
            else
                stmt.setNull(8, Types.INTEGER);
            stmt.setInt(9, p.getIdProduit());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idProduit) throws SQLException {
        String sql = "DELETE FROM Produit WHERE id_produit=?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    public Produit rechercherParId(int idProduit) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.id_produit = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToProduit(rs);
                throw new ProduitIntrouvableException(idProduit);
            }
        }
    }

    /**
     * Recherche un produit via son code-barres (utilisé pour le scan en vente).
     */
    public Produit rechercherParCodeBarre(String codeBarre) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.code_barre = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, codeBarre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToProduit(rs);
                throw new ProduitIntrouvableException("CODE_BARRE", codeBarre);
            }
        }
    }

    public List<Produit> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.nom LIKE ? ORDER BY p.nom";
        List<Produit> produits = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    public List<Produit> listerTous() throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "ORDER BY p.nom";
        List<Produit> produits = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                produits.add(mapResultSetToProduit(rs));
        }
        return produits;
    }

    /**
     * Liste les produits dont le stock est inférieur ou égal au seuil d'alerte.
     */
    public List<Produit> listerProduitsEnAlerte() throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.quantite_stock <= p.seuil_alerte ORDER BY p.quantite_stock";
        List<Produit> produits = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                produits.add(mapResultSetToProduit(rs));
        }
        return produits;
    }

    public boolean mettreAJourStock(int idProduit, int nouvelleQuantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = ? WHERE id_produit = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nouvelleQuantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean diminuerStock(int idProduit, int quantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = quantite_stock - ? WHERE id_produit = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean augmenterStock(int idProduit, int quantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = quantite_stock + ? WHERE id_produit = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Identifie les produits qui vont expirer dans les 'jours' à venir.
     */
    public List<Produit> listerProduitsExpirantBientot(int jours) throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.date_expiration IS NOT NULL AND p.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY p.date_expiration";
        List<Produit> produits = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, jours);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    /**
     * Transforme une ligne de ResultSet en objet Produit.
     * Récupère aussi le nom du fournisseur s'il est présent dans la jointure.
     */
    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        Produit p = new Produit(
                rs.getInt("id_produit"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getString("code_barre"),
                rs.getBigDecimal("prix_unitaire"),
                rs.getInt("quantite_stock"),
                rs.getInt("seuil_alerte"),
                rs.getDate("date_expiration") != null ? rs.getDate("date_expiration").toLocalDate() : null,
                rs.getInt("id_fournisseur") != 0 ? rs.getInt("id_fournisseur") : null);

        try {
            p.setNomFournisseur(rs.getString("nom_fournisseur"));
        } catch (SQLException ignored) {
        }
        return p;
    }
}
