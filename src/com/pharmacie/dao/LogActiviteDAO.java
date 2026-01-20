package com.pharmacie.dao;

import com.pharmacie.model.LogActivite;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les logs d'activité
 * Gère la persistance des logs dans la base de données
 */
public class LogActiviteDAO {

    private DBConnection dbConnection;

    /**
     * Constructeur - Initialise la connexion à la base de données
     */
    public LogActiviteDAO() throws ConnexionEchoueeException {
        this.dbConnection = DBConnection.getInstance();
    }

    /**
     * Ajoute un nouveau log d'activité
     */
    public boolean ajouter(LogActivite log) throws SQLException {
        String query = "INSERT INTO LogActivite (date_action, type_action, description, id_utilisateur) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setTimestamp(1, Timestamp.valueOf(log.getDateAction()));
                stmt.setString(2, log.getTypeAction());
                stmt.setString(3, log.getDescription());
                if (log.getIdUtilisateur() != null)
                    stmt.setInt(4, log.getIdUtilisateur());
                else
                    stmt.setNull(4, Types.INTEGER);
                return stmt.executeUpdate() > 0;
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Récupère un log par ID
     */
    public LogActivite rechercherParId(int idLog) throws SQLException {
        String query = "SELECT * FROM LogActivite WHERE id_log = ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, idLog);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapperResultSet(rs);
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion rechercherParId(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL rechercherParId(): " + e.getMessage());
            throw e;
        }

        return null;
    }

    /**
     * Liste tous les logs
     */
    public List<LogActivite> listerTous() throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite ORDER BY date_action DESC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    logs.add(mapperResultSet(rs));
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion listerTous(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL listerTous(): " + e.getMessage());
            throw e;
        }

        return logs;
    }

    /**
     * Liste les logs par type d'action
     */
    public List<LogActivite> listerParType(String typeAction) throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite WHERE type_action = ? ORDER BY date_action DESC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, typeAction);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapperResultSet(rs));
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion listerParType(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL listerParType(): " + e.getMessage());
            throw e;
        }

        return logs;
    }

    /**
     * Liste les logs d'un utilisateur
     */
    public List<LogActivite> listerParUtilisateur(int idUtilisateur) throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite WHERE id_utilisateur = ? ORDER BY date_action DESC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, idUtilisateur);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapperResultSet(rs));
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion listerParUtilisateur(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL listerParUtilisateur(): " + e.getMessage());
            throw e;
        }

        return logs;
    }

    /**
     * Liste les logs par période
     */
    public List<LogActivite> listerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite WHERE date_action BETWEEN ? AND ? ORDER BY date_action DESC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setTimestamp(1, Timestamp.valueOf(debut));
                stmt.setTimestamp(2, Timestamp.valueOf(fin));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapperResultSet(rs));
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion listerParPeriode(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL listerParPeriode(): " + e.getMessage());
            throw e;
        }

        return logs;
    }

    /**
     * Supprime les logs anciens (plus de X jours)
     */
    public int supprimerLogsAnciens(int jours) throws SQLException {
        String query = "DELETE FROM LogActivite WHERE date_action < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, jours);
                return stmt.executeUpdate();
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion supprimerLogsAnciens(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        }
    }

    /**
     * Supprime un log par ID
     */
    public boolean supprimer(int idLog) throws SQLException {
        String query = "DELETE FROM LogActivite WHERE id_log = ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, idLog);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion supprimer(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL supprimer(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Compte le nombre total de logs
     */
    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM LogActivite";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Mappe un ResultSet vers une entité LogActivite
     */
    private LogActivite mapperResultSet(ResultSet rs) throws SQLException {
        LogActivite log = new LogActivite();
        log.setIdLog(rs.getInt("id_log"));
        log.setDateAction(rs.getTimestamp("date_action").toLocalDateTime());
        log.setTypeAction(rs.getString("type_action"));
        log.setDescription(rs.getString("description"));
        int idUtilisateur = rs.getInt("id_utilisateur");
        log.setIdUtilisateur(rs.wasNull() ? null : idUtilisateur);
        return log;
    }
}