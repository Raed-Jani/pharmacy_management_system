package com.pharmacie.dao;

import com.pharmacie.model.Vente;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VenteDAO {

    private Connection manualConnection;

    public VenteDAO() {
    }

    public VenteDAO(Connection connection) {
        this.manualConnection = connection;
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

    /**
     * Enregistre une nouvelle vente et récupère l'ID généré pour le suivi.
     */
    public boolean ajouter(Vente vente) throws SQLException {
        String query = "INSERT INTO Vente (date_vente, total_vente, id_client, id_utilisateur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
    }

    /**
     * Recherche une vente par son ID.
     * Jointure avec Client et Utilisateur pour avoir les noms au lieu de simples
     * IDs.
     */
    public Vente rechercherParId(int idVente) throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur WHERE v.id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    public List<Vente> listerTous() throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur ORDER BY v.date_vente DESC";
        return fetchList(query, null);
    }

    public List<Vente> listerParClient(int idClient) throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_client = ? ORDER BY v.date_vente DESC";
        return fetchList(query, idClient);
    }

    public List<Vente> listerParUtilisateur(int idUtilisateur) throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.id_utilisateur = ? ORDER BY v.date_vente DESC";
        return fetchList(query, idUtilisateur);
    }

    public List<Vente> listerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String query = "SELECT v.*, COALESCE(c.nom, 'Anonyme') as nom_client, u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "WHERE v.date_vente BETWEEN ? AND ? ORDER BY v.date_vente DESC";
        List<Vente> ventes = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    ventes.add(mapperResultSet(rs));
            }
        }
        return ventes;
    }

    private List<Vente> fetchList(String query, Object param) throws SQLException {
        List<Vente> list = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            if (param != null)
                stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapperResultSet(rs));
            }
        }
        return list;
    }

    public boolean modifier(Vente vente) throws SQLException {
        String query = "UPDATE Vente SET date_vente = ?, total_vente = ?, id_client = ?, id_utilisateur = ? WHERE id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
            stmt.setBigDecimal(2, vente.getTotalVente());
            if (vente.getIdClient() != null)
                stmt.setInt(3, vente.getIdClient());
            else
                stmt.setNull(3, Types.INTEGER);
            stmt.setInt(4, vente.getIdUtilisateur());
            stmt.setInt(5, vente.getIdVente());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idVente) throws SQLException {
        String query = "DELETE FROM Vente WHERE id_vente = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idVente);
            return stmt.executeUpdate() > 0;
        }
    }

    public double calculerChiffreAffairesTotal() throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getDouble("total") : 0.0;
        }
    }

    public double calculerChiffreAffairesPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente WHERE date_vente BETWEEN ? AND ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        }
    }

    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM Vente";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

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
     * Récupère le top des produits vendus (format: "Nom (Quantité)").
     */
    public List<String> getTopProduits(int limit) throws SQLException {
        List<String> result = new ArrayList<>();
        String query = "SELECT p.nom, SUM(lv.quantite_vendue) as total FROM LigneVente lv " +
                "JOIN Produit p ON lv.id_produit = p.id_produit " +
                "GROUP BY p.id_produit, p.nom ORDER BY total DESC LIMIT ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    result.add(rs.getString("nom") + " (" + rs.getInt("total") + ")");
            }
        }
        return result;
    }

    /**
     * Calcule le chiffre d'affaires quotidien des derniers 'jours'.
     */
    public List<String> getCAJournalier(int jours) throws SQLException {
        List<String> result = new ArrayList<>();
        String query = "SELECT DATE(date_vente) as jour, SUM(total_vente) as total FROM Vente " +
                "WHERE date_vente >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(date_vente) ORDER BY jour ASC";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, jours);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    result.add(rs.getDate("jour").toString() + ": " + rs.getBigDecimal("total") + " TND");
            }
        }
        return result;
    }
}
