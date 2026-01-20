package com.pharmacie.dao;

import com.pharmacie.model.Client;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les clients
 */
public class ClientDAO {

    private DBConnection dbConnection;

    public ClientDAO() throws ConnexionEchoueeException {
        this.dbConnection = DBConnection.getInstance();
    }

    private Connection getConnection() throws SQLException {
        try {
            return dbConnection.getAdminConnection();
        } catch (ConnexionEchoueeException e) {
            throw new SQLException("Erreur de connexion", e);
        }
    }

    /**
     * Ajoute un nouveau client
     */
    public boolean ajouter(Client client) throws SQLException {
        String query = "INSERT INTO Client (nom, prenom, telephone, email, historique_medical) VALUES (?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getTelephone());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getHistoriqueMedical());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        client.setIdClient(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Récupère un client par ID
     */
    public Client rechercherParId(int idClient) throws SQLException {
        String query = "SELECT * FROM Client WHERE id_client = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idClient);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    /**
     * Liste tous les clients
     */
    public List<Client> listerTous() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Client ORDER BY nom, prenom";
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next())
                clients.add(mapperResultSet(rs));
        }
        return clients;
    }

    /**
     * Recherche un client par nom
     */
    public List<Client> rechercherParNom(String nom) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Client WHERE nom LIKE ? ORDER BY nom, prenom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    clients.add(mapperResultSet(rs));
            }
        }
        return clients;
    }

    /**
     * Recherche un client par email
     */
    public Client rechercherParEmail(String email) throws SQLException {
        String query = "SELECT * FROM Client WHERE email = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    /**
     * Recherche un client par téléphone
     */
    public Client rechercherParTelephone(String telephone) throws SQLException {
        String query = "SELECT * FROM Client WHERE telephone = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, telephone);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    /**
     * Modifie un client existant
     */
    public boolean modifier(Client client) throws SQLException {
        String query = "UPDATE Client SET nom = ?, prenom = ?, telephone = ?, email = ?, historique_medical = ? WHERE id_client = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getTelephone());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getHistoriqueMedical());
            stmt.setInt(6, client.getIdClient());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un client
     */
    public boolean supprimer(int idClient) throws SQLException {
        String query = "DELETE FROM Client WHERE id_client = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idClient);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Compte le nombre total de clients
     */
    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM Client";
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    /**
     * Mappe un ResultSet vers une entité Client
     */
    private Client mapperResultSet(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setIdClient(rs.getInt("id_client"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setTelephone(rs.getString("telephone"));
        client.setEmail(rs.getString("email"));
        client.setHistoriqueMedical(rs.getString("historique_medical"));
        return client;
    }
}