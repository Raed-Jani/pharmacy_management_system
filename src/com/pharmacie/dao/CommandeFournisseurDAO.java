package com.pharmacie.dao;

import com.pharmacie.model.CommandeFournisseur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeFournisseurDAO {

    private Connection manualConnection;

    public CommandeFournisseurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    public CommandeFournisseurDAO() {
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

    public boolean ajouter(CommandeFournisseur cmd) throws SQLException {
        String sql = "INSERT INTO CommandeFournisseur (date_creation, date_reception, statut, id_fournisseur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(cmd.getDateCreation()));
            stmt.setTimestamp(2, cmd.getDateReception() != null ? Timestamp.valueOf(cmd.getDateReception()) : null);
            stmt.setString(3, cmd.getStatut());
            stmt.setInt(4, cmd.getIdFournisseur());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        cmd.setIdCommande(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public boolean modifier(CommandeFournisseur cmd) throws SQLException {
        String sql = "UPDATE CommandeFournisseur SET date_reception = ?, statut = ? WHERE id_commande = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, cmd.getDateReception() != null ? Timestamp.valueOf(cmd.getDateReception()) : null);
            stmt.setString(2, cmd.getStatut());
            stmt.setInt(3, cmd.getIdCommande());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean modifierStatut(int idCommande, String statut, java.time.LocalDateTime dateReception)
            throws SQLException {
        String sql = "UPDATE CommandeFournisseur SET statut = ?, date_reception = ? WHERE id_commande = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setTimestamp(2, dateReception != null ? Timestamp.valueOf(dateReception) : null);
            stmt.setInt(3, idCommande);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idCommande) throws SQLException {
        String sql = "DELETE FROM CommandeFournisseur WHERE id_commande = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            return stmt.executeUpdate() > 0;
        }
    }

    public CommandeFournisseur rechercherParId(int idCommande) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur WHERE c.id_commande = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToCommande(rs) : null;
            }
        }
    }

    public List<CommandeFournisseur> listerTous() throws SQLException {
        String sql = "SELECT c.*, f.nom_societe FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur ORDER BY c.date_creation DESC";
        return fetchList(sql, null);
    }

    public List<CommandeFournisseur> listerParStatut(String statut) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur WHERE c.statut = ? ORDER BY c.date_creation DESC";
        return fetchList(sql, statut);
    }

    public List<CommandeFournisseur> listerParFournisseur(int idFournisseur) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur WHERE c.id_fournisseur = ? ORDER BY c.date_creation DESC";
        return fetchList(sql, idFournisseur);
    }

    private List<CommandeFournisseur> fetchList(String sql, Object param) throws SQLException {
        List<CommandeFournisseur> list = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            if (param != null)
                stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSetToCommande(rs));
            }
        }
        return list;
    }

    private CommandeFournisseur mapResultSetToCommande(ResultSet rs) throws SQLException {
        Timestamp dateRec = rs.getTimestamp("date_reception");
        CommandeFournisseur c = new CommandeFournisseur(
                rs.getInt("id_commande"),
                rs.getTimestamp("date_creation").toLocalDateTime(),
                dateRec != null ? dateRec.toLocalDateTime() : null,
                rs.getString("statut"),
                rs.getInt("id_fournisseur"));
        c.setNomFournisseur(rs.getString("nom_societe"));
        return c;
    }
}
