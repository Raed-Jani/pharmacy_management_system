package com.pharmacie.dao;

import com.pharmacie.model.Vente;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les ventes.
 */
public class VenteDAO {

    private Connection connection;

    public VenteDAO(Connection connection) {
        this.connection = connection;
    }

    public VenteDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getEmployeConnection();
    }

    /**
     * Ajoute une nouvelle vente.
     */
    public boolean ajouter(Vente vente) throws SQLException {
        String sql = "INSERT INTO Vente (date_vente, total_vente, id_client, id_utilisateur) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
            stmt.setBigDecimal(2, vente.getTotalVente());

            if (vente.getIdClient() != null) {
                stmt.setInt(3, vente.getIdClient());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setInt(4, vente.getIdUtilisateur());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    vente.setIdVente(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie une vente existante.
     */
    public boolean modifier(Vente vente) throws SQLException {
        String sql = "UPDATE Vente SET total_vente = ?, id_client = ? WHERE id_vente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, vente.getTotalVente());

            if (vente.getIdClient() != null) {
                stmt.setInt(2, vente.getIdClient());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setInt(3, vente.getIdVente());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime une vente par son ID.
     */
    public boolean supprimer(int idVente) throws SQLException {
        String sql = "DELETE FROM Vente WHERE id_vente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVente);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche une vente par son ID.
     */
    public Vente rechercherParId(int idVente) throws SQLException {
        String sql = "SELECT v.*, c.nom, c.prenom, u.login " +
                "FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_vente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToVente(rs);
            }
            return null;
        }
    }

    /**
     * Récupère toutes les ventes.
     */
    public List<Vente> listerTous() throws SQLException {
        String sql = "SELECT v.*, c.nom, c.prenom, u.login " +
                "FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "ORDER BY v.date_vente DESC";
        List<Vente> ventes = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }
        }
        return ventes;
    }

    /**
     * Récupère les ventes d'un client spécifique.
     */
    public List<Vente> listerParClient(int idClient) throws SQLException {
        String sql = "SELECT v.*, c.nom, c.prenom, u.login " +
                "FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_client = ? ORDER BY v.date_vente DESC";
        List<Vente> ventes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }
        }
        return ventes;
    }

    /**
     * Récupère les ventes effectuées par un utilisateur.
     */
    public List<Vente> listerParUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT v.*, c.nom, c.prenom, u.login " +
                "FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_utilisateur = ? ORDER BY v.date_vente DESC";
        List<Vente> ventes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }
        }
        return ventes;
    }

    /**
     * Récupère les ventes d'une période donnée.
     */
    public List<Vente> listerParPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) throws SQLException {
        String sql = "SELECT v.*, c.nom, c.prenom, u.login " +
                "FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.date_vente BETWEEN ? AND ? ORDER BY v.date_vente DESC";
        List<Vente> ventes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }
        }
        return ventes;
    }

    /**
     * Calcule le chiffre d'affaires total.
     */
    public double calculerChiffreAffairesTotal() throws SQLException {
        String sql = "SELECT SUM(total_vente) FROM Vente";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    /**
     * Calcule le CA d'une période.
     */
    public double calculerChiffreAffairesPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) throws SQLException {
        String sql = "SELECT SUM(total_vente) FROM Vente WHERE date_vente BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    /**
     * Compte le nombre total de ventes.
     */
    public int compterVentes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vente";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Vente.
     */
    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        Vente vente = new Vente(
                rs.getInt("id_vente"),
                rs.getTimestamp("date_vente").toLocalDateTime(),
                rs.getBigDecimal("total_vente"),
                (Integer) rs.getObject("id_client"),
                rs.getInt("id_utilisateur")
        );

        // Ajout des informations relationnelles si disponibles
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        if (nom != null && prenom != null) {
            vente.setNomClient(prenom + " " + nom);
        }

        String login = rs.getString("login");
        if (login != null) {
            vente.setLoginUtilisateur(login);
        }

        return vente;
    }
}