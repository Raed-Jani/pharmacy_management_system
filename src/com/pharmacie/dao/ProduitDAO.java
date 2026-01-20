package com.pharmacie.dao;

import com.pharmacie.model.Produit;
import com.pharmacie.exception.ProduitIntrouvableException;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les produits.
 */
public class ProduitDAO {

    private DBConnection dbConnection;
    private Connection manualConnection;

    /**
     * Constructeur avec connexion spécifique (pour compatibilité).
     */
    public ProduitDAO(Connection connection) {
        this.manualConnection = connection;
    }

    /**
     * Constructeur par défaut utilisant le singleton DBConnection.
     */
    public ProduitDAO() throws ConnexionEchoueeException {
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
     * Ajoute un nouveau produit dans la base de données.
     */
    public boolean ajouter(Produit produit) throws SQLException {
        String sql = "INSERT INTO Produit (nom, description, code_barre, prix_unitaire, quantite_stock, seuil_alerte, date_expiration, id_fournisseur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setString(3, produit.getCodeBarre());
            stmt.setBigDecimal(4, produit.getPrixUnitaire());
            stmt.setInt(5, produit.getQuantiteStock());
            stmt.setInt(6, produit.getSeuilAlerte());
            stmt.setDate(7, produit.getDateExpiration() != null ? Date.valueOf(produit.getDateExpiration()) : null);
            if (produit.getIdFournisseur() != null) {
                stmt.setInt(8, produit.getIdFournisseur());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        produit.setIdProduit(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie un produit existant.
     */
    public boolean modifier(Produit produit) throws SQLException {
        String sql = "UPDATE Produit SET nom = ?, description = ?, code_barre = ?, " +
                "prix_unitaire = ?, quantite_stock = ?, seuil_alerte = ?, date_expiration = ?, id_fournisseur = ? WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setString(3, produit.getCodeBarre());
            stmt.setBigDecimal(4, produit.getPrixUnitaire());
            stmt.setInt(5, produit.getQuantiteStock());
            stmt.setInt(6, produit.getSeuilAlerte());
            stmt.setDate(7, produit.getDateExpiration() != null ? Date.valueOf(produit.getDateExpiration()) : null);
            if (produit.getIdFournisseur() != null) {
                stmt.setInt(8, produit.getIdFournisseur());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setInt(9, produit.getIdProduit());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un produit par son ID.
     */
    public boolean supprimer(int idProduit) throws SQLException {
        String sql = "DELETE FROM Produit WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche un produit par son ID.
     */
    public Produit rechercherParId(int idProduit) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduit(rs);
                } else {
                    throw new ProduitIntrouvableException(idProduit);
                }
            }
        }
    }

    /**
     * Recherche un produit par son code-barres.
     */
    public Produit rechercherParCodeBarre(String codeBarre) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.code_barre = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeBarre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduit(rs);
                } else {
                    throw new ProduitIntrouvableException("CODE_BARRE", codeBarre);
                }
            }
        }
    }

    /**
     * Recherche des produits par nom (recherche partielle).
     */
    public List<Produit> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.nom LIKE ? ORDER BY p.nom";
        List<Produit> produits = new ArrayList<>();

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        }
        return produits;
    }

    /**
     * Récupère tous les produits.
     */
    public List<Produit> listerTous() throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "ORDER BY p.nom";
        List<Produit> produits = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    /**
     * Récupère les produits en alerte de stock.
     */
    public List<Produit> listerProduitsEnAlerte() throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.quantite_stock <= p.seuil_alerte ORDER BY p.quantite_stock";
        List<Produit> produits = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    /**
     * Met à jour uniquement le stock d'un produit.
     */
    public boolean mettreAJourStock(int idProduit, int nouvelleQuantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = ? WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nouvelleQuantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Diminue le stock d'un produit (lors d'une vente).
     */
    public boolean diminuerStock(int idProduit, int quantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = quantite_stock - ? WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Augmente le stock d'un produit (lors d'une réception de commande).
     */
    public boolean augmenterStock(int idProduit, int quantite) throws SQLException {
        String sql = "UPDATE Produit SET quantite_stock = quantite_stock + ? WHERE id_produit = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Produit> listerProduitsExpirantBientot(int jours) throws SQLException {
        String sql = "SELECT p.*, f.nom_societe as nom_fournisseur FROM Produit p " +
                "LEFT JOIN Fournisseur f ON p.id_fournisseur = f.id_fournisseur " +
                "WHERE p.date_expiration IS NOT NULL AND p.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY p.date_expiration";
        List<Produit> produits = new ArrayList<>();

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jours);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        }
        return produits;
    }

    /**
     * Mappe un ResultSet vers un objet Produit.
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
        } catch (SQLException e) {
            // nom_fournisseur might not be in the row if the join was omitted
        }
        return p;
    }
}