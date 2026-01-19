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

    private Connection connection;

    public CommandeFournisseurDAO(Connection connection) {
        this.connection = connection;
    }

    public CommandeFournisseurDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getAdminConnection();
    }

    /**
     * Ajoute une nouvelle commande.
     */
    public boolean ajouter(CommandeFournisseur commande) throws SQLException {
        String sql = "INSERT INTO CommandeFournisseur (date_creation, date_reception, statut, id_fournisseur) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(commande.getDateCreation()));

            if (commande.getDateReception() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(commande.getDateReception()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }

            stmt.setString(3, commande.getStatut());
            stmt.setInt(4, commande.getIdFournisseur());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
     * Supprime une commande par son ID.
     */
    public boolean supprimer(int idCommande) throws SQLException {
        String sql = "DELETE FROM CommandeFournisseur WHERE id_commande = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche une commande par son ID.
     */
    public CommandeFournisseur rechercherParId(int idCommande) throws SQLException {
        String sql = "SELECT c.*, f.nom_societe " +
                "FROM CommandeFournisseur c " +
                "JOIN Fournisseur f ON c.id_fournisseur = f.id_fournisseur " +
                "WHERE c.id_commande = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommande);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCommande(rs);
            }
            return null;
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

        try (Statement stmt = connection.createStatement();
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
            }
        }
        return commandes;
    }

    /**
     * Mappe un ResultSet vers un objet CommandeFournisseur.
     */
    private CommandeFournisseur mapResultSetToCommande(ResultSet rs) throws SQLException {
        Timestamp dateReception = rs.getTimestamp("date_reception");

        CommandeFournisseur commande = new CommandeFournisseur(
                rs.getInt("id_commande"),
                rs.getTimestamp("date_creation").toLocalDateTime(),
                dateReception != null ? dateReception.toLocalDateTime() : null,
                rs.getString("statut"),
                rs.getInt("id_fournisseur")
        );

        String nomFournisseur = rs.getString("nom_societe");
        if (nomFournisseur != null) {
            commande.setNomFournisseur(nomFournisseur);
        }

        return commande;
    }
}
