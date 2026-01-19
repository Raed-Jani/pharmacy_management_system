package com.pharmacie.dao;

import com.pharmacie.model.LigneVente;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les lignes de vente.
 */
public class LigneVenteDAO {

    private Connection connection;

    public LigneVenteDAO(Connection connection) {
        this.connection = connection;
    }

    public LigneVenteDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getEmployeConnection();
    }

    /**
     * Ajoute une ligne de vente.
     */
    public boolean ajouter(LigneVente ligne) throws SQLException {
        String sql = "INSERT INTO LigneVente (id_vente, id_produit, quantite_vendue, prix_applique) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ligne.getIdVente());
            stmt.setInt(2, ligne.getIdProduit());
            stmt.setInt(3, ligne.getQuantiteVendue());
            stmt.setBigDecimal(4, ligne.getPrixApplique());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    ligne.setIdLigneVente(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Récupère toutes les lignes d'une vente.
     */
    public List<LigneVente> listerParVente(int idVente) throws SQLException {
        String sql = "SELECT lv.*, p.nom " +
                "FROM LigneVente lv " +
                "JOIN Produit p ON lv.id_produit = p.id_produit " +
                "WHERE lv.id_vente = ?";
        List<LigneVente> lignes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lignes.add(mapResultSetToLigneVente(rs));
            }
        }
        return lignes;
    }

    /**
     * Supprime une ligne de vente.
     */
    public boolean supprimer(int idLigneVente) throws SQLException {
        String sql = "DELETE FROM LigneVente WHERE id_ligne_vente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idLigneVente);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Récupère les produits les plus vendus.
     */
    public List<LigneVente> getProduitsLesPlusVendus(int limite) throws SQLException {
        String sql = "SELECT lv.id_produit, p.nom, " +
                "SUM(lv.quantite_vendue) as total_vendu, " +
                "SUM(lv.quantite_vendue * lv.prix_applique) as ca_genere " +
                "FROM LigneVente lv " +
                "JOIN Produit p ON lv.id_produit = p.id_produit " +
                "GROUP BY lv.id_produit, p.nom " +
                "ORDER BY total_vendu DESC LIMIT ?";
        List<LigneVente> lignes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LigneVente ligne = new LigneVente();
                ligne.setIdProduit(rs.getInt("id_produit"));
                ligne.setNomProduit(rs.getString("nom"));
                ligne.setQuantiteVendue(rs.getInt("total_vendu"));
                lignes.add(ligne);
            }
        }
        return lignes;
    }

    /**
     * Mappe un ResultSet vers un objet LigneVente.
     */
    private LigneVente mapResultSetToLigneVente(ResultSet rs) throws SQLException {
        LigneVente ligne = new LigneVente(
                rs.getInt("id_ligne_vente"),
                rs.getInt("id_vente"),
                rs.getInt("id_produit"),
                rs.getInt("quantite_vendue"),
                rs.getBigDecimal("prix_applique")
        );

        String nomProduit = rs.getString("nom");
        if (nomProduit != null) {
            ligne.setNomProduit(nomProduit);
        }

        return ligne;
    }
}