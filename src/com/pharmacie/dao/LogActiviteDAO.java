package com.pharmacie.dao;

import com.pharmacie.model.LogActivite;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogActiviteDAO {

    private Connection manualConnection;

    public LogActiviteDAO() {
    }

    public LogActiviteDAO(Connection connection) {
        this.manualConnection = connection;
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

    public boolean ajouter(LogActivite log) throws SQLException {
        String query = "INSERT INTO LogActivite (date_action, type_action, description, id_utilisateur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(log.getDateAction()));
            stmt.setString(2, log.getTypeAction());
            stmt.setString(3, log.getDescription());
            if (log.getIdUtilisateur() != null)
                stmt.setInt(4, log.getIdUtilisateur());
            else
                stmt.setNull(4, Types.INTEGER);
            return stmt.executeUpdate() > 0;
        }
    }

    public LogActivite rechercherParId(int idLog) throws SQLException {
        String query = "SELECT * FROM LogActivite WHERE id_log = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idLog);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    public List<LogActivite> listerTous() throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite ORDER BY date_action DESC";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next())
                logs.add(mapperResultSet(rs));
        }
        return logs;
    }

    public List<LogActivite> listerParType(String typeAction) throws SQLException {
        String query = "SELECT * FROM LogActivite WHERE type_action = ? ORDER BY date_action DESC";
        return listerWithQuery(query, typeAction);
    }

    public List<LogActivite> listerParUtilisateur(int idUtilisateur) throws SQLException {
        String query = "SELECT * FROM LogActivite WHERE id_utilisateur = ? ORDER BY date_action DESC";
        return listerWithQuery(query, idUtilisateur);
    }

    private List<LogActivite> listerWithQuery(String query, Object param) throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            if (param instanceof String)
                stmt.setString(1, (String) param);
            else if (param instanceof Integer)
                stmt.setInt(1, (Integer) param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    logs.add(mapperResultSet(rs));
            }
        }
        return logs;
    }

    public List<LogActivite> listerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        List<LogActivite> logs = new ArrayList<>();
        String query = "SELECT * FROM LogActivite WHERE date_action BETWEEN ? AND ? ORDER BY date_action DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    logs.add(mapperResultSet(rs));
            }
        }
        return logs;
    }

    public int supprimerLogsAnciens(int jours) throws SQLException {
        String query = "DELETE FROM LogActivite WHERE date_action < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, jours);
            return stmt.executeUpdate();
        }
    }

    public boolean supprimer(int idLog) throws SQLException {
        String query = "DELETE FROM LogActivite WHERE id_log = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idLog);
            return stmt.executeUpdate() > 0;
        }
    }

    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM LogActivite";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

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
