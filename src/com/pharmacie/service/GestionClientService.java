package com.pharmacie.service;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.model.Client;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.SQLException;
import java.util.List;

public class GestionClientService {

    private ClientDAO clientDAO;

    public GestionClientService() throws ConnexionEchoueeException {
        this.clientDAO = new ClientDAO();
    }

    public List<Client> listerTous() throws SQLException {
        return clientDAO.listerTous();
    }

    public Client rechercherParId(int id) throws SQLException {
        return clientDAO.rechercherParId(id);
    }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        return clientDAO.rechercherParNom(nom); // Assuming DAO has this, otherwise filter in service
    }

    public boolean ajouterClient(Client client) throws SQLException {
        // Business logic validation could go here
        if (client.getNom() == null || client.getNom().isEmpty()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire");
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
