package com.pharmacie.service;

import com.pharmacie.dao.VenteDAO;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service gérant les statistiques du tableau de bord.
 * Regroupe les calculs de CA, le suivi des stocks et les performances produits.
 */
public class DashboardStatisticsService {

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public BigDecimal getTotalRevenue() throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) FROM Vente";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public BigDecimal getRevenueForPeriod(LocalDate start, LocalDate end) throws SQLException {
        String query = "SELECT COALESCE(SUM(total_vente), 0) FROM Vente WHERE DATE(date_vente) BETWEEN ? AND ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Calcule le chiffre d'affaires mensuel sur une année précise.
     * Retourne une map triée par mois.
     */
    public Map<YearMonth, BigDecimal> getMonthlyRevenue(int year) throws SQLException {
        Map<YearMonth, BigDecimal> results = new LinkedHashMap<>();
        String query = "SELECT MONTH(date_vente) as month, SUM(total_vente) as total " +
                "FROM Vente WHERE YEAR(date_vente) = ? GROUP BY month ORDER BY month";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.put(YearMonth.of(year, rs.getInt("month")), rs.getBigDecimal("total"));
                }
            }
        }
        return results;
    }

    /**
     * Récupère le classement des produits les plus vendus.
     * 
     * @param limit Nombre de produits à retourner.
     */
    public Map<String, Integer> getTopSellingProducts(int limit) throws SQLException {
        Map<String, Integer> results = new LinkedHashMap<>();
        String query = "SELECT p.nom, SUM(lv.quantite_vendue) as qty FROM Produit p " +
                "JOIN LigneVente lv ON p.id_produit = lv.id_produit GROUP BY p.id_produit, p.nom ORDER BY qty DESC LIMIT ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    results.put(rs.getString("nom"), rs.getInt("qty"));
            }
        }
        return results;
    }

    /**
     * Synthèse de l'état du stock (total, ruptures, alertes, expirations proches).
     * Utilise des agrégations conditionnelles pour limiter les appels SQL.
     */
    public Map<String, Object> getStockSummary() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String query = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN quantite_stock = 0 THEN 1 ELSE 0 END) as outOfStock, " +
                "SUM(CASE WHEN quantite_stock <= seuil_alerte AND quantite_stock > 0 THEN 1 ELSE 0 END) as lowStock, " +
                "SUM(CASE WHEN date_expiration <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as expiringSoon "
                +
                "FROM Produit";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                stats.put("totalProducts", rs.getInt("total"));
                stats.put("outOfStock", rs.getInt("outOfStock"));
                stats.put("lowStock", rs.getInt("lowStock"));
                stats.put("expiringSoon", rs.getInt("expiringSoon"));
            }
        }
        return stats;
    }

    public List<Map<String, Object>> getAlertProducts() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT nom, quantite_stock, seuil_alerte FROM Produit WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("nom", rs.getString("nom"));
                p.put("stock", rs.getInt("quantite_stock"));
                p.put("seuil", rs.getInt("seuil_alerte"));
                results.add(p);
            }
        }
        return results;
    }

    /**
     * Liste les dernières ventes réalisées avec le nom du client (ou Guest si
     * anonyme).
     */
    public List<Map<String, Object>> getRecentSales(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT v.id_vente, v.date_vente, v.total_vente, COALESCE(c.nom, 'Guest') as client " +
                "FROM Vente v LEFT JOIN Client c ON v.id_client = c.id_client ORDER BY v.date_vente DESC LIMIT ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> v = new HashMap<>();
                    v.put("id", rs.getInt("id_vente"));
                    v.put("date", rs.getTimestamp("date_vente").toLocalDateTime());
                    v.put("total", rs.getBigDecimal("total_vente"));
                    v.put("client", rs.getString("client"));
                    results.add(v);
                }
            }
        }
        return results;
    }

    public int getPendingOrdersCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM CommandeFournisseur WHERE statut = 'EN_COURS'";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<String> getDailyRevenueHistory(int days) throws SQLException {
        VenteDAO vdao = new VenteDAO(getConnection());
        return vdao.getCAJournalier(days);
    }
}
