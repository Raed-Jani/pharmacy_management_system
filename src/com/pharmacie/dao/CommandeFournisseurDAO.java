package com.pharmacie.dao;

import com.pharmacie.model.CommandeFournisseur;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les commandes fournisseurs.
 */
public class CommandeFournisseurDAO {

    private DBConnection dbConnection;
    private Connection manualConnection;

    /**
     * Constructeur avec connexion spécifique (pour compatibilité).
     */
    public CommandeFournisseurDAO(Connection connection) {
        this.manualConnection = connection;
    }

    /**
     * Constructeur utilisant le singleton DBConnection.
     */
    public CommandeFournisseurDAO() throws ConnexionEchoueeException {
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
     * Ajoute une nouvelle commande.
     */
    public boolean ajouter(CommandeFournisseur commande) throws SQLException {
        String sql = "INSERT INTO CommandeFournisseur (date_creation, date_reception, statut, id_fournisseur) VALUES (?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(commande.getDateCreation()));
            stmt.setTimestamp(2,
                    commande.getDateReception() != null ? Timestamp.valueOf(commande.getDateReception()) : null);
            stmt.setString(3, commande.getStatut());
            stmt.setInt(4, commande.getIdFournisseur());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        commande.setIdCommande(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie une commande existante.
     */
    public boolean modifier(CommandeFournisseur commande) throws SQLException {
        String sql = "UPDATE CommandeFournisseur SET date_reception = ?, statut = ? WHERE id_commande = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (commande.getDateReception() != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(commande.getDateReception()));
            } else {
                stmt.setNull(1, Types.TIMESTAMP);
            }

            stmt.setString(2, commande.getStatut());
            stmt.setInt(3, commande.getIdCommande());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Modifie le statut d'une commande.
     */
    public boolean modifierStatut(int idCommande, String statut, java.time.LocalDateTime dateReception)
            throws SQLException {
        String sql = "UPDATE CommandeFournisseur SET statut = ?, date_reception = ? WHERE id_commande = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);

            if (dateReception != null) {
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(dateReception));
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            }

            stmt.setInt(3, idCommande);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime une commande par son ID.
     */
    public boolean supprimer(int idCommande) throws SQLException {
        String sql = "DELETE FROM CommandeFournisseur WHERE id_commande = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche une commande par son ID.
     */
    public CommandeFournisseur rechercherParId(int idCommande) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur WHERE c.id_commande = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToCommande(rs) : null;
            }
        }
    }

    /**
     * Récupère toutes les commandes.
     */
    public List<CommandeFournisseur> listerTous() throws SQLException {
        String sql = "SELECT c.*, f.nom_societe " +
                "FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur " +
                "ORDER BY c.date_creation DESC";
        List<CommandeFournisseur> commandes = new ArrayList<>();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
            }
        }
        return commandes;
    }

    /**
     * Récupère les commandes par statut.
     */
    public List<CommandeFournisseur> listerParStatut(String statut) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe " +
                "FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur " +
                "WHERE c.statut = ? ORDER BY c.date_creation DESC";
        List<CommandeFournisseur> commandes = new ArrayList<>();

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapResultSetToCommande(rs));
                }
            }
        }
        return commandes;
    }

    /**
     * Récupère les commandes d'un fournisseur.
     */
    public List<CommandeFournisseur> listerParFournisseur(int idFournisseur) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe " +
                "FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur " +
                "WHERE c.id_fournisseur = ? ORDER BY c.date_creation DESC";
        List<CommandeFournisseur> commandes = new ArrayList<>();

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapResultSetToCommande(rs));
                }
            }
        }
        return commandes;
    }

    /**
     * Mappe un ResultSet vers un objet CommandeFournisseur.
     */
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
