package com.pharmacie.dao;

import com.pharmacie.model.Fournisseur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les fournisseurs.
 */
public class FournisseurDAO {

    private Connection connection;

    public FournisseurDAO(Connection connection) {
        this.connection = connection;
    }

    public FournisseurDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getAdminConnection();
    }

    /**
     * Ajoute un nouveau fournisseur.
     */
    public boolean ajouter(Fournisseur fournisseur) throws SQLException {
        String sql = "INSERT INTO Fournisseur (nom_societe, adresse, telephone, email) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fournisseur.getNomSociete());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    fournisseur.setIdFournisseur(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie un fournisseur existant.
     */
    public boolean modifier(Fournisseur fournisseur) throws SQLException {
        String sql = "UPDATE Fournisseur SET nom_societe = ?, adresse = ?, telephone = ?, email = ? WHERE id_fournisseur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fournisseur.getNomSociete());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setInt(5, fournisseur.getIdFournisseur());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un fournisseur par son ID.
     */
    public boolean supprimer(int idFournisseur) throws SQLException {
        String sql = "DELETE FROM Fournisseur WHERE id_fournisseur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche un fournisseur par son ID.
     */
    public Fournisseur rechercherParId(int idFournisseur) throws SQLException {
        String sql = "SELECT * FROM Fournisseur WHERE id_fournisseur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFournisseur(rs);
            }
            return null;
        }
    }

    /**
     * Recherche des fournisseurs par nom.
     */
    public List<Fournisseur> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT * FROM Fournisseur WHERE nom_societe LIKE ? ORDER BY nom_societe";
        List<Fournisseur> fournisseurs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fournisseurs.add(mapResultSetToFournisseur(rs));
            }
        }
        return fournisseurs;
    }

    /**
     * Récupère tous les fournisseurs.
     */
    public List<Fournisseur> listerTous() throws SQLException {
        String sql = "SELECT * FROM Fournisseur ORDER BY nom_societe";
        List<Fournisseur> fournisseurs = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fournisseurs.add(mapResultSetToFournisseur(rs));
            }
        }
        return fournisseurs;
    }

    /**
     * Compte le nombre total de fournisseurs.
     */
    public int compterFournisseurs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Fournisseur";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Fournisseur.
     */
    private Fournisseur mapResultSetToFournisseur(ResultSet rs) throws SQLException {
        return new Fournisseur(
                rs.getInt("id_fournisseur"),
                rs.getString("nom_societe"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                rs.getString("email")
        );
    }
}
