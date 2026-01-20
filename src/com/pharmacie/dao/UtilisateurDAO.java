package com.pharmacie.dao;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les utilisateurs.
 */
public class UtilisateurDAO {

    private DBConnection dbConnection;
    private Connection manualConnection;

    /**
     * Constructeur avec connexion spécifique (pour compatibilité).
     */
    public UtilisateurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    /**
     * Constructeur utilisant le singleton DBConnection.
     */
    public UtilisateurDAO() throws ConnexionEchoueeException {
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
     * Ajoute un nouvel utilisateur.
     */
    public boolean ajouter(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO Utilisateur (login, mot_de_passe, nom, prenom, role) VALUES (?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getLogin());
            stmt.setString(2, utilisateur.getMotDePasse());
            stmt.setString(3, utilisateur.getNom());
            stmt.setString(4, utilisateur.getPrenom());
            stmt.setString(5, utilisateur.getRole());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        utilisateur.setIdUtilisateur(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie un utilisateur existant.
     */
    public boolean modifier(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE Utilisateur SET login = ?, mot_de_passe = ?, nom = ?, prenom = ?, role = ? WHERE id_utilisateur = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getLogin());
            stmt.setString(2, utilisateur.getMotDePasse());
            stmt.setString(3, utilisateur.getNom());
            stmt.setString(4, utilisateur.getPrenom());
            stmt.setString(5, utilisateur.getRole());
            stmt.setInt(6, utilisateur.getIdUtilisateur());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un utilisateur par son ID.
     */
    public boolean supprimer(int idUtilisateur) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id_utilisateur = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche un utilisateur par son ID.
     */
    public Utilisateur rechercherParId(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToUtilisateur(rs) : null;
            }
        }
    }

    /**
     * Recherche un utilisateur par son login.
     */
    public Utilisateur rechercherParLogin(String login) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE login = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
                return null;
            }
        }
    }

    /**
     * Authentifie un utilisateur (vérifie login + mot de passe).
     */
    public Utilisateur authentifier(String login, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE login = ? AND mot_de_passe = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, motDePasse);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
                return null;
            }
        }
    }

    /**
     * Récupère tous les utilisateurs.
     */
    public List<Utilisateur> listerTous() throws SQLException {
        String sql = "SELECT * FROM Utilisateur ORDER BY login";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        }
        return utilisateurs;
    }

    /**
     * Récupère tous les administrateurs.
     */
    public List<Utilisateur> listerAdministrateurs() throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE role = 'ADMIN' ORDER BY login";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        }
        return utilisateurs;
    }

    /**
     * Récupère tous les employés.
     */
    public List<Utilisateur> listerEmployes() throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE role = 'EMPLOYE' ORDER BY login";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        }
        return utilisateurs;
    }

    /**
     * Vérifie si un login existe déjà.
     */
    public boolean loginExiste(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE login = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Change le mot de passe d'un utilisateur.
     */
    public boolean changerMotDePasse(int idUtilisateur, String nouveauMotDePasse) throws SQLException {
        String sql = "UPDATE Utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nouveauMotDePasse);
            stmt.setInt(2, idUtilisateur);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Compte le nombre total d'utilisateurs.
     */
    public int compterUtilisateurs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Utilisateur";
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Utilisateur.
     */
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