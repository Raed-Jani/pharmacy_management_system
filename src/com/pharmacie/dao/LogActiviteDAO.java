package com.pharmacie.dao;

import com.pharmacie.model.LogActivite;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les logs d'activité.
 */
public class LogActiviteDAO {

    private Connection connection;

    public LogActiviteDAO(Connection connection) {
        this.connection = connection;
    }

    public LogActiviteDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getEmployeConnection();
    }

    /**
     * Ajoute un nouveau log.
     */
    public boolean ajouter(LogActivite log) throws SQLException {
        String sql = "INSERT INTO LogActivite (date_action, type_action, description, id_utilisateur) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(log.getDateAction()));
            stmt.setString(2, log.getTypeAction());
            stmt.setString(3, log.getDescription());

            if (log.getIdUtilisateur() != null) {
                stmt.setInt(4, log.getIdUtilisateur());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    log.setIdLog(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Récupère tous les logs.
     */
    public List<LogActivite> listerTous() throws SQLException {
        String sql = "SELECT l.*, u.login " +
                "FROM LogActivite l " +
                "LEFT JOIN Utilisateur u ON l.id_utilisateur = u.id_utilisateur " +
                "ORDER BY l.date_action DESC";
        List<LogActivite> logs = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    /**
     * Récupère les logs par type d'action.
     */
    public List<LogActivite> listerParType(String typeAction) throws SQLException {
        String sql = "SELECT l.*, u.login " +
                "FROM LogActivite l " +
                "LEFT JOIN Utilisateur u ON l.id_utilisateur = u.id_utilisateur " +
                "WHERE l.type_action = ? ORDER BY l.date_action DESC";
        List<LogActivite> logs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, typeAction);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    /**
     * Récupère les logs d'un utilisateur.
     */
    public List<LogActivite> listerParUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT l.*, u.login " +
                "FROM LogActivite l " +
                "LEFT JOIN Utilisateur u ON l.id_utilisateur = u.id_utilisateur " +
                "WHERE l.id_utilisateur = ? ORDER BY l.date_action DESC";
        List<LogActivite> logs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    /**
     * Récupère les logs d'une période.
     */
    public List<LogActivite> listerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String sql = "SELECT l.*, u.login " +
                "FROM LogActivite l " +
                "LEFT JOIN Utilisateur u ON l.id_utilisateur = u.id_utilisateur " +
                "WHERE l.date_action BETWEEN ? AND ? ORDER BY l.date_action DESC";
        List<LogActivite> logs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    /**
     * Mappe un ResultSet vers un objet LogActivite.
     */
    private LogActivite mapResultSetToLog(ResultSet rs) throws SQLException {
        LogActivite log = new LogActivite(
                rs.getInt("id_log"),
                rs.getTimestamp("date_action").toLocalDateTime(),
                rs.getString("type_action"),
                rs.getString("description"),
                (Integer) rs.getObject("id_utilisateur")
        );

        String login = rs.getString("login");
        if (login != null) {
            log.setLoginUtilisateur(login);
        }

        return log;
    }
}