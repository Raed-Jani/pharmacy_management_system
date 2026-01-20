package com.pharmacie.dao;

import com.pharmacie.model.Vente;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les ventes
 */
public class VenteDAO {

    private DBConnection dbConnection;

    public VenteDAO() throws ConnexionEchoueeException {
        this.dbConnection = DBConnection.getInstance();
    }

    /**
     * Ajoute une nouvelle vente
     */
    public boolean ajouter(Vente vente) throws SQLException {
        String query = "INSERT INTO Vente (date_vente, total_vente, id_client, id_utilisateur) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
                stmt.setBigDecimal(2, vente.getTotalVente());
                if (vente.getIdClient() != null)
                    stmt.setInt(3, vente.getIdClient());
                else
                    stmt.setNull(3, Types.INTEGER);
                stmt.setInt(4, vente.getIdUtilisateur());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next())
                            vente.setIdVente(rs.getInt(1));
                    }
                    return true;
                }
                return false;
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Récupère une vente par ID
     */
    public Vente rechercherParId(int idVente) throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Client anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur WHERE v.id_vente = ?";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idVente);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? mapperResultSet(rs) : null;
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Liste toutes les ventes
     */
    public List<Vente> listerTous() throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String query = "SELECT v.*, COALESCE(c.nom, 'Client anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur ORDER BY v.date_vente DESC";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next())
                    ventes.add(mapperResultSet(rs));
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return ventes;
    }

    /**
     * Liste les ventes d'un client
     */
    public List<Vente> listerParClient(int idClient) throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String query = "SELECT v.*, COALESCE(c.nom, 'Client anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_client = ? ORDER BY v.date_vente DESC";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idClient);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next())
                        ventes.add(mapperResultSet(rs));
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return ventes;
    }

    /**
     * Liste les ventes d'un utilisateur
     */
    public List<Vente> listerParUtilisateur(int idUtilisateur) throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String query = "SELECT v.*, COALESCE(c.nom, 'Client anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_utilisateur = ? ORDER BY v.date_vente DESC";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idUtilisateur);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next())
                        ventes.add(mapperResultSet(rs));
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return ventes;
    }

    /**
     * Liste les ventes par période
     */
    public List<Vente> listerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String query = "SELECT v.*, COALESCE(c.nom, 'Client anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v. id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v. id_utilisateur = u.id_utilisateur " +
                "WHERE v.date_vente BETWEEN ? AND ? ORDER BY v.date_vente DESC";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setTimestamp(1, Timestamp.valueOf(debut));
                stmt.setTimestamp(2, Timestamp.valueOf(fin));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next())
                        ventes.add(mapperResultSet(rs));
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return ventes;
    }

    /**
     * Modifie une vente existante
     */
    public boolean modifier(Vente vente) throws SQLException {
        String query = "UPDATE Vente SET date_vente = ?, total_vente = ?, id_client = ?, id_utilisateur = ? " +
                "WHERE id_vente = ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
                stmt.setBigDecimal(2, vente.getTotalVente());

                if (vente.getIdClient() != null) {
                    stmt.setInt(3, vente.getIdClient());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }

                stmt.setInt(4, vente.getIdUtilisateur());
                stmt.setInt(5, vente.getIdVente());

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion modifier(): " + e.getMessage());
            throw new SQLException("Erreur de connexion", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL modifier(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime une vente
     */
    public boolean supprimer(int idVente) throws SQLException {
        String query = "DELETE FROM Vente WHERE id_vente = ?";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idVente);
                return stmt.executeUpdate() > 0;
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Calcule le chiffre d'affaires total
     */
    public double calculerChiffreAffairesTotal() throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Calcule le chiffre d'affaires pour une période
     */
    public double calculerChiffreAffairesPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente WHERE date_vente BETWEEN ? AND ?";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setTimestamp(1, Timestamp.valueOf(debut));
                stmt.setTimestamp(2, Timestamp.valueOf(fin));
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getDouble("total") : 0.0;
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Compte le nombre total de ventes
     */
    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM Vente";
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
     * Mappe un ResultSet vers une entité Vente
     */
    private Vente mapperResultSet(ResultSet rs) throws SQLException {
        Vente vente = new Vente();
        vente.setIdVente(rs.getInt("id_vente"));
        vente.setDateVente(rs.getTimestamp("date_vente").toLocalDateTime());
        vente.setTotalVente(rs.getBigDecimal("total_vente"));
        int idClient = rs.getInt("id_client");
        vente.setIdClient(rs.wasNull() ? null : idClient);
        vente.setIdUtilisateur(rs.getInt("id_utilisateur"));
        vente.setNomClient(rs.getString("nom_client"));
        vente.setLoginUtilisateur(rs.getString("login"));
        return vente;
    }

    /**
     * Récupère le Top X des produits les plus vendus.
     */
    public List<String> getTopProduits(int limit) throws SQLException {
        List<String> result = new ArrayList<>();
        // Note: Assuming V_Top_Produits exists or using raw query
        String query = "SELECT p.nom, SUM(lv.quantite_vendue) as total " +
                "FROM LigneVente lv " +
                "JOIN Produit p ON lv.id_produit = p.id_produit " +
                "GROUP BY p.id_produit, p.nom " +
                "ORDER BY total DESC LIMIT ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getString("nom") + " (" + rs.getInt("total") + ")");
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur connexion stats", e);
        }
        return result;
    }

    /**
     * Récupère le CA par jour pour les X derniers jours.
     */
    public List<String> getCAJournalier(int jours) throws SQLException {
        List<String> result = new ArrayList<>();
        String query = "SELECT DATE(date_vente) as jour, SUM(total_vente) as total " +
                "FROM Vente " +
                "WHERE date_vente >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(date_vente) " +
                "ORDER BY jour DESC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, jours);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getDate("jour").toString() + ": " + rs.getBigDecimal("total") + " TND");
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur connexion stats", e);
        }
        return result;
    }
}
