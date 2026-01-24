package com.pharmacie.service;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.model.Client;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing clients.
 */
public class GestionClientService {

    private final ClientDAO clientDAO;

    public GestionClientService() {
        this.clientDAO = new ClientDAO();
    }

    public List<Client> listerTous() throws SQLException {
        return clientDAO.listerTous();
    }

    public Client rechercherParId(int id) throws SQLException {
        return clientDAO.rechercherParId(id);
    }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        return clientDAO.rechercherParNom(nom);
    }

    public boolean ajouterClient(Client client) throws SQLException {
        if (client.getNom() == null || client.getNom().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        return clientDAO.ajouter(client);
    }

    public boolean modifierClient(Client client) throws SQLException {
        return clientDAO.modifier(client);
    }

    public boolean supprimerClient(int id) throws SQLException {
        return clientDAO.supprimer(id);
    }
}
