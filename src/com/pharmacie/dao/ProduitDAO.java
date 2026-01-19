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

    private Connection connection;

    /**
     * Constructeur avec connexion spécifique.
     */
    public ProduitDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Constructeur utilisant la connexion admin par défaut.
     */
    public ProduitDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getAdminConnection();
    }

    /**
     * Ajoute un nouveau produit dans la base de données.
     */
    public boolean ajouter(Produit produit) throws SQLException {
        String sql = "INSERT INTO Produit (nom, description, code_barre, prix_unitaire, quantite_stock, seuil_alerte) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setString(3, produit.getCodeBarre());
            stmt.setBigDecimal(4, produit.getPrixUnitaire());
            stmt.setInt(5, produit.getQuantiteStock());
            stmt.setInt(6, produit.getSeuilAlerte());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
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
                "prix_unitaire = ?, quantite_stock = ?, seuil_alerte = ? WHERE id_produit = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setString(3, produit.getCodeBarre());
            stmt.setBigDecimal(4, produit.getPrixUnitaire());
            stmt.setInt(5, produit.getQuantiteStock());
            stmt.setInt(6, produit.getSeuilAlerte());
            stmt.setInt(7, produit.getIdProduit());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un produit par son ID.
     */
    public boolean supprimer(int idProduit) throws SQLException {
        String sql = "DELETE FROM Produit WHERE id_produit = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche un produit par son ID.
     */
    public Produit rechercherParId(int idProduit) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT * FROM Produit WHERE id_produit = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduit(rs);
            } else {
                throw new ProduitIntrouvableException(idProduit);
            }
        }
    }

    /**
     * Recherche un produit par son code-barres.
     */
    public Produit rechercherParCodeBarre(String codeBarre) throws SQLException, ProduitIntrouvableException {
        String sql = "SELECT * FROM Produit WHERE code_barre = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codeBarre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduit(rs);
            } else {
                throw new ProduitIntrouvableException("CODE_BARRE", codeBarre);
            }
        }
    }

    /**
     * Recherche des produits par nom (recherche partielle).
     */
    public List<Produit> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT * FROM Produit WHERE nom LIKE ? ORDER BY nom";
        List<Produit> produits = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    /**
     * Récupère tous les produits.
     */
    public List<Produit> listerTous() throws SQLException {
        String sql = "SELECT * FROM Produit ORDER BY nom";
        List<Produit> produits = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
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
        String sql = "SELECT * FROM Produit WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock";
        List<Produit> produits = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Compte le nombre total de produits.
     */
    public int compterProduits() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Produit";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Produit.
     */
    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        return new Produit(
                rs.getInt("id_produit"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getString("code_barre"),
                rs.getBigDecimal("prix_unitaire"),
                rs.getInt("quantite_stock"),
                rs.getInt("seuil_alerte")
        );
    }
}