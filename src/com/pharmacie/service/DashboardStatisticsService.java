package com.pharmacie.service;

import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service pour gérer les statistiques du tableau de bord.
 * Fournit des données pour les rapports et les visualisations.
 */
public class DashboardStatisticsService {

    private DBConnection dbConnection;
    private boolean isConnected = false;

    public DashboardStatisticsService() throws ConnexionEchoueeException {
        try {
            this.dbConnection = DBConnection.getInstance();
            // Tester la connexion immédiatement
            dbConnection.getAdminConnection();
            this.isConnected = true;
            System.out.println("✓ DashboardStatisticsService:  Connexion établie");
        } catch (ConnexionEchoueeException e) {
            this.isConnected = false;
            System.err.println("✗ DashboardStatisticsService: Erreur de connexion");
            System.err.println("  Message:  " + e.getMessage());
            System.err.println("  Aide: " + e.getMessageAide());
            throw e;
        } catch (Exception e) {
            this.isConnected = false;
            System.err.println("✗ DashboardStatisticsService:  Erreur inattendue");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
            throw new ConnexionEchoueeException(
                    "Erreur lors de l'initialisation du service",
                    "Vérifiez que MySQL est démarré et accessible");
        }
    }

    /**
     * Vérifie si le service est connecté
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Récupère le chiffre d'affaires total
     */
    public BigDecimal getChiffreAffairesTotal() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
                return BigDecimal.ZERO;
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getChiffreAffairesTotal(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getChiffreAffairesTotal(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère le chiffre d'affaires pour une période donnée
     */
    public BigDecimal getChiffreAffairesPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        String query = "SELECT COALESCE(SUM(total_vente), 0) as total FROM Vente " +
                "WHERE DATE(date_vente) BETWEEN ? AND ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setDate(1, java.sql.Date.valueOf(debut));
                stmt.setDate(2, java.sql.Date.valueOf(fin));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("total");
                    }
                    return BigDecimal.ZERO;
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getChiffreAffairesPeriode(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getChiffreAffairesPeriode(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère le chiffre d'affaires par mois
     */
    public Map<YearMonth, BigDecimal> getChiffreAffairesParMois(int annee) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        Map<YearMonth, BigDecimal> resultats = new LinkedHashMap<>();

        String query = "SELECT YEAR(date_vente) as annee, MONTH(date_vente) as mois, " +
                "COALESCE(SUM(total_vente), 0) as total " +
                "FROM Vente WHERE YEAR(date_vente) = ? " +
                "GROUP BY YEAR(date_vente), MONTH(date_vente) " +
                "ORDER BY mois";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, annee);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        YearMonth ym = YearMonth.of(rs.getInt("annee"), rs.getInt("mois"));
                        BigDecimal total = rs.getBigDecimal("total");
                        resultats.put(ym, total);
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getChiffreAffairesParMois(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getChiffreAffairesParMois(): " + e.getMessage());
            throw e;
        }

        return resultats;
    }

    /**
     * Récupère le nombre de ventes
     */
    public int getNombreVentes() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        String query = "SELECT COUNT(*) as nombre FROM Vente";
        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                if (rs.next()) {
                    return rs.getInt("nombre");
                }
                return 0;
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getNombreVentes(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getNombreVentes(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère le nombre de ventes pour une période donnée
     */
    public int getNombreVentesPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        String query = "SELECT COUNT(*) as nombre FROM Vente " +
                "WHERE DATE(date_vente) BETWEEN ? AND ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setDate(1, java.sql.Date.valueOf(debut));
                stmt.setDate(2, java.sql.Date.valueOf(fin));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("nombre");
                    }
                    return 0;
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getNombreVentesPeriode(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getNombreVentesPeriode(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère les produits les plus vendus
     */
    public Map<String, Integer> getProduitsTopVentes(int limite) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        Map<String, Integer> resultats = new LinkedHashMap<>();

        String query = "SELECT p.nom, COALESCE(SUM(lv.quantite_vendue), 0) as quantite_vendue " +
                "FROM Produit p " +
                "LEFT JOIN LigneVente lv ON p. id_produit = lv. id_produit " +
                "GROUP BY p.id_produit, p.nom " +
                "ORDER BY quantite_vendue DESC LIMIT ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, limite);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        resultats.put(rs.getString("nom"),
                                rs.getInt("quantite_vendue"));
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getProduitsTopVentes(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getProduitsTopVentes(): " + e.getMessage());
            throw e;
        }

        return resultats;
    }

    /**
     * Récupère la performance des fournisseurs
     */
    public Map<String, Map<String, Object>> getPerformanceFournisseurs() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        Map<String, Map<String, Object>> resultats = new LinkedHashMap<>();

        String query = "SELECT f.id_fournisseur, f. nom_societe, " +
                "COUNT(c.id_commande) as total_commandes, " +
                "SUM(CASE WHEN c.statut = 'RECUE' THEN 1 ELSE 0 END) as livrees, " +
                "COALESCE(SUM(lc.quantite_commandee * lc.prix_achat), 0) as montant_total " +
                "FROM Fournisseur f " +
                "LEFT JOIN CommandeFournisseur c ON f.id_fournisseur = c.id_fournisseur " +
                "LEFT JOIN LigneCommande lc ON c.id_commande = lc.id_commande " +
                "GROUP BY f.id_fournisseur, f.nom_societe";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalCommandes", rs.getInt("total_commandes"));
                    stats.put("livrees", rs.getInt("livrees"));
                    stats.put("montantTotal", rs.getBigDecimal("montant_total"));

                    // Calculer le taux de livraison
                    int total = rs.getInt("total_commandes");
                    int livrees = rs.getInt("livrees");
                    double tauxLivraison = total > 0 ? (livrees * 100.0 / total) : 0;
                    stats.put("tauxLivraison", tauxLivraison);

                    resultats.put(rs.getString("nom_societe"), stats);
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getPerformanceFournisseurs(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getPerformanceFournisseurs(): " + e.getMessage());
            throw e;
        }

        return resultats;
    }

    /**
     * Récupère l'état des stocks
     */
    public Map<String, Object> getEtatStocks() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        Map<String, Object> stats = new HashMap<>();

        String query = "SELECT " +
                "COUNT(*) as total_produits, " +
                "SUM(CASE WHEN quantite_stock = 0 THEN 1 ELSE 0 END) as rupture, " +
                "SUM(CASE WHEN quantite_stock <= seuil_alerte THEN 1 ELSE 0 END) as en_alerte, " +
                "SUM(CASE WHEN date_expiration IS NOT NULL AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as expirant_proche, "
                +
                "COALESCE(SUM(quantite_stock), 0) as quantite_totale, " +
                "COALESCE(SUM(quantite_stock * prix_unitaire), 0) as valeur_stock " +
                "FROM Produit";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                if (rs.next()) {
                    stats.put("totalProduits", rs.getInt("total_produits"));
                    stats.put("rupture", rs.getInt("rupture"));
                    stats.put("enAlerte", rs.getInt("en_alerte"));
                    stats.put("expirantProche", rs.getInt("expirant_proche"));
                    stats.put("quantiteTotale", rs.getInt("quantite_totale"));
                    stats.put("valeurStock", rs.getBigDecimal("valeur_stock"));
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getEtatStocks(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getEtatStocks(): " + e.getMessage());
            throw e;
        }

        return stats;
    }

    /**
     * Récupère les produits expirant dans les X prochains jours
     */
    public List<Map<String, Object>> getProduitsExpirantBientot(int jours) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        List<Map<String, Object>> resultats = new ArrayList<>();

        String query = "SELECT id_produit, nom, date_expiration, quantite_stock " +
                "FROM Produit WHERE date_expiration IS NOT NULL " +
                "AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY date_expiration ASC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, jours);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", rs.getInt("id_produit"));
                        p.put("nom", rs.getString("nom"));
                        p.put("expiration", rs.getDate("date_expiration").toLocalDate());
                        p.put("stock", rs.getInt("quantite_stock"));
                        resultats.add(p);
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return resultats;
    }

    /**
     * Récupère les produits en rupture de stock
     */
    public List<Map<String, Object>> getProduitsEnRupture() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        List<Map<String, Object>> resultats = new ArrayList<>();

        String query = "SELECT id_produit, nom, prix_unitaire, quantite_stock " +
                "FROM Produit WHERE quantite_stock = 0 ORDER BY nom";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Map<String, Object> produit = new HashMap<>();
                    produit.put("id", rs.getInt("id_produit"));
                    produit.put("nom", rs.getString("nom"));
                    produit.put("prix", rs.getBigDecimal("prix_unitaire"));
                    resultats.add(produit);
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getProduitsEnRupture(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getProduitsEnRupture(): " + e.getMessage());
            throw e;
        }

        return resultats;
    }

    /**
     * Récupère les produits dont le stock est inférieur ou égal au seuil d'alerte
     */
    public List<Map<String, Object>> getProduitsEnAlerte() throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        List<Map<String, Object>> resultats = new ArrayList<>();

        String query = "SELECT id_produit, nom, quantite_stock, seuil_alerte " +
                "FROM Produit WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock ASC";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Map<String, Object> produit = new HashMap<>();
                    produit.put("id", rs.getInt("id_produit"));
                    produit.put("nom", rs.getString("nom"));
                    produit.put("stock", rs.getInt("quantite_stock"));
                    produit.put("seuil", rs.getInt("seuil_alerte"));
                    resultats.add(produit);
                }
            }
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
        return resultats;
    }

    /**
     * Récupère les ventes récentes
     */
    public List<Map<String, Object>> getVentesRecentes(int limite) throws SQLException {
        if (!isConnected) {
            throw new SQLException("Service non connecté à la base de données");
        }

        List<Map<String, Object>> resultats = new ArrayList<>();

        String query = "SELECT v.id_vente, v.date_vente, v.total_vente, " +
                "COALESCE(CONCAT(c.prenom, ' ', c.nom), 'Client anonyme') as nom_client, " +
                "u.login FROM Vente v " +
                "LEFT JOIN Client c ON v.id_client = c.id_client " +
                "LEFT JOIN Utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
                "ORDER BY v.date_vente DESC LIMIT ?";

        try {
            Connection conn = dbConnection.getAdminConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, limite);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> vente = new HashMap<>();
                        vente.put("id", rs.getInt("id_vente"));
                        vente.put("date", rs.getTimestamp("date_vente").toLocalDateTime());
                        vente.put("total", rs.getBigDecimal("total_vente"));
                        vente.put("client", rs.getString("nom_client"));
                        vente.put("employe", rs.getString("login"));
                        resultats.add(vente);
                    }
                }
            }
        } catch (ConnexionEchoueeException e) {
            System.err.println("✗ Erreur connexion getVentesRecentes(): " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL getVentesRecentes(): " + e.getMessage());
            throw e;
        }

        return resultats;
    }
}