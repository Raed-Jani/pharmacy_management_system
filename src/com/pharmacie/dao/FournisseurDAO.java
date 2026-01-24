package com.pharmacie.dao;

import com.pharmacie.model.Fournisseur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {

    private Connection manualConnection;

    public FournisseurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    public FournisseurDAO() {
    }

    private Connection getConnection() throws SQLException {
        if (manualConnection != null)
            return manualConnection;
        try {
            return DBConnection.getConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException(e);
        }
    }

    public boolean ajouter(Fournisseur fournisseur) throws SQLException {
        String sql = "INSERT INTO Fournisseur (nom_societe, adresse, telephone, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fournisseur.getNomSociete());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        fournisseur.setIdFournisseur(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public boolean modifier(Fournisseur fournisseur) throws SQLException {
        String sql = "UPDATE Fournisseur SET nom_societe = ?, adresse = ?, telephone = ?, email = ? WHERE id_fournisseur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, fournisseur.getNomSociete());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setInt(5, fournisseur.getIdFournisseur());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idFournisseur) throws SQLException {
        String sql = "DELETE FROM Fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            return stmt.executeUpdate() > 0;
        }
    }

    public Fournisseur rechercherParId(int idFournisseur) throws SQLException {
        String sql = "SELECT * FROM Fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToFournisseur(rs) : null;
            }
        }
    }

    public List<Fournisseur> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT * FROM Fournisseur WHERE nom_societe LIKE ? ORDER BY nom_societe";
        List<Fournisseur> fournisseurs = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    fournisseurs.add(mapResultSetToFournisseur(rs));
            }
        }
        return fournisseurs;
    }

    public List<Fournisseur> listerTous() throws SQLException {
        String sql = "SELECT * FROM Fournisseur ORDER BY nom_societe";
        List<Fournisseur> fournisseurs = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                fournisseurs.add(mapResultSetToFournisseur(rs));
        }
        return fournisseurs;
    }

    public int compterFournisseurs() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Fournisseur";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private Fournisseur mapResultSetToFournisseur(ResultSet rs) throws SQLException {
        return new Fournisseur(
                rs.getInt("id_fournisseur"),
                rs.getString("nom_societe"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                rs.getString("email"));
    }
}
