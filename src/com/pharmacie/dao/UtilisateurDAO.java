package com.pharmacie.dao;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Connection manualConnection;

    public UtilisateurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    public UtilisateurDAO() {
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

    public boolean ajouter(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO Utilisateur (login, mot_de_passe, nom, prenom, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getMotDePasse());
            stmt.setString(3, user.getNom());
            stmt.setString(4, user.getPrenom());
            stmt.setString(5, user.getRole());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        user.setIdUtilisateur(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public boolean modifier(Utilisateur user) throws SQLException {
        String sql = "UPDATE Utilisateur SET login=?, mot_de_passe=?, nom=?, prenom=?, role=? WHERE id_utilisateur=?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getMotDePasse());
            stmt.setString(3, user.getNom());
            stmt.setString(4, user.getPrenom());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getIdUtilisateur());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idUtilisateur) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            return stmt.executeUpdate() > 0;
        }
    }

    public Utilisateur rechercherParId(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToUtilisateur(rs) : null;
            }
        }
    }

    public Utilisateur rechercherParLogin(String login) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE login = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToUtilisateur(rs) : null;
            }
        }
    }

    public Utilisateur authentifier(String login, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE login = ? AND mot_de_passe = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, motDePasse);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToUtilisateur(rs) : null;
            }
        }
    }

    public List<Utilisateur> listerTous() throws SQLException {
        return listerParRole(null);
    }

    public List<Utilisateur> listerAdministrateurs() throws SQLException {
        return listerParRole("ADMIN");
    }

    public List<Utilisateur> listerEmployes() throws SQLException {
        return listerParRole("EMPLOYE");
    }

    private List<Utilisateur> listerParRole(String role) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Utilisateur");
        if (role != null)
            sql.append(" WHERE role = ?");
        sql.append(" ORDER BY login");

        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql.toString())) {
            if (role != null)
                stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        }
        return utilisateurs;
    }

    public boolean loginExiste(String login) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Utilisateur WHERE login = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    public boolean changerMotDePasse(int idUtilisateur, String nouveauMotDePasse) throws SQLException {
        String sql = "UPDATE Utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, nouveauMotDePasse);
            stmt.setInt(2, idUtilisateur);
            return stmt.executeUpdate() > 0;
        }
    }

    public int compterUtilisateurs() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Utilisateur";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id_utilisateur"),
                rs.getString("login"),
                rs.getString("mot_de_passe"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("role"));
    }
}
