package com.pharmacie.dao;

import com.pharmacie.model.Client;
import com.pharmacie.exception.ConnexionEchoueeException;
import com.pharmacie.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les clients.
 */
public class ClientDAO {

    private Connection connection;

    public ClientDAO(Connection connection) {
        this.connection = connection;
    }

    public ClientDAO() throws ConnexionEchoueeException {
        this.connection = DBConnection.getInstance().getEmployeConnection();
    }

    /**
     * Ajoute un nouveau client.
     */
    public boolean ajouter(Client client) throws SQLException {
        String sql = "INSERT INTO Client (nom, prenom, telephone, email, historique_medical) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getTelephone());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getHistoriqueMedical());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    client.setIdClient(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Modifie un client existant.
     */
    public boolean modifier(Client client) throws SQLException {
        String sql = "UPDATE Client SET nom = ?, prenom = ?, telephone = ?, " +
                "email = ?, historique_medical = ? WHERE id_client = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
     * Supprime un client par son ID.
     */
    public boolean supprimer(int idClient) throws SQLException {
        String sql = "DELETE FROM Client WHERE id_client = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idClient);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recherche un client par son ID.
     */
    public Client rechercherParId(int idClient) throws SQLException {
        String sql = "SELECT * FROM Client WHERE id_client = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            return null;
        }
    }

    /**
     * Recherche des clients par nom.
     */
    public List<Client> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT * FROM Client WHERE nom LIKE ? OR prenom LIKE ? ORDER BY nom, prenom";
        List<Client> clients = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String pattern = "%" + nom + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    /**
     * Recherche un client par téléphone.
     */
    public Client rechercherParTelephone(String telephone) throws SQLException {
        String sql = "SELECT * FROM Client WHERE telephone = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telephone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            return null;
        }
    }

    /**
     * Récupère tous les clients.
     */
    public List<Client> listerTous() throws SQLException {
        String sql = "SELECT * FROM Client ORDER BY nom, prenom";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    /**
     * Récupère les clients avec historique médical.
     */
    public List<Client> listerAvecHistoriqueMedical() throws SQLException {
        String sql = "SELECT * FROM Client WHERE historique_medical IS NOT NULL " +
                "AND historique_medical != '' ORDER BY nom, prenom";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    /**
     * Compte le nombre total de clients.
     */
    public int compterClients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Client";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Client.
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id_client"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("telephone"),
                rs.getString("email"),
                rs.getString("historique_medical")
        );
    }
}