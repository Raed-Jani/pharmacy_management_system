package com.pharmacie.dao;

import com.pharmacie.model.Client;
import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private Connection manualConnection;

    public ClientDAO() {
    }

    public ClientDAO(Connection connection) {
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

    public boolean ajouter(Client client) throws SQLException {
        String query = "INSERT INTO Client (nom, prenom, telephone, email, historique_medical) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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

    public Client rechercherParId(int idClient) throws SQLException {
        String query = "SELECT * FROM Client WHERE id_client = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idClient);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    public List<Client> listerTous() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Client ORDER BY nom, prenom";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next())
                clients.add(mapperResultSet(rs));
        }
        return clients;
    }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Client WHERE nom LIKE ? ORDER BY nom, prenom";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    clients.add(mapperResultSet(rs));
            }
        }
        return clients;
    }

    public Client rechercherParEmail(String email) throws SQLException {
        String query = "SELECT * FROM Client WHERE email = ?";
        return findOne(query, email);
    }

    public Client rechercherParTelephone(String telephone) throws SQLException {
        String query = "SELECT * FROM Client WHERE telephone = ?";
        return findOne(query, telephone);
    }

    private Client findOne(String query, String param) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperResultSet(rs) : null;
            }
        }
    }

    public boolean modifier(Client client) throws SQLException {
        String query = "UPDATE Client SET nom = ?, prenom = ?, telephone = ?, email = ?, historique_medical = ? WHERE id_client = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getTelephone());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getHistoriqueMedical());
            stmt.setInt(6, client.getIdClient());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimer(int idClient) throws SQLException {
        String query = "DELETE FROM Client WHERE id_client = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, idClient);
            return stmt.executeUpdate() > 0;
        }
    }

    public int compterTous() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM Client";
        try (Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

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
