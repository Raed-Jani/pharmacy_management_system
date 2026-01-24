package com.pharmacie.dao;

import com.pharmacie.model.LigneCommandeFournisseur;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeFournisseurDAO {

    private Connection manualConnection;

    public LigneCommandeFournisseurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    public LigneCommandeFournisseurDAO() {
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

    public void ajouter(LigneCommandeFournisseur ligne) throws SQLException {
        String sql = "INSERT INTO LigneCommande (id_commande, id_produit, quantite_commandee, prix_achat) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ligne.getIdCommande());
            stmt.setInt(2, ligne.getIdProduit());
            stmt.setInt(3, ligne.getQuantiteCommandee());
            stmt.setBigDecimal(4, ligne.getPrixAchat());
            stmt.executeUpdate();
        }
    }

    public List<LigneCommandeFournisseur> listerParCommande(int idCommande) throws SQLException {
        String sql = "SELECT lc.*, p.nom AS nom_produit, p.code_barre FROM LigneCommande lc " +
                "JOIN Produit p ON lc.id_produit = p.id_produit WHERE lc.id_commande = ?";
        List<LigneCommandeFournisseur> lignes = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LigneCommandeFournisseur l = new LigneCommandeFournisseur();
                    l.setIdLigneCmd(rs.getInt("id_ligne_cmd"));
                    l.setIdCommande(rs.getInt("id_commande"));
                    l.setIdProduit(rs.getInt("id_produit"));
                    l.setQuantiteCommandee(rs.getInt("quantite_commandee"));
                    l.setPrixAchat(rs.getBigDecimal("prix_achat"));
                    l.setNomProduit(rs.getString("nom_produit"));
                    l.setCodeBarre(rs.getString("code_barre"));
                    lignes.add(l);
                }
            }
        }
        return lignes;
    }

    public void supprimerParCommande(int idCommande) throws SQLException {
        String sql = "DELETE FROM LigneCommande WHERE id_commande = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            stmt.executeUpdate();
        }
    }
}
