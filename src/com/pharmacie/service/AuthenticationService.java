package com.pharmacie.service;

import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.model.LogActivite;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuthenticationService {

    private static AuthenticationService instance;
    private Utilisateur utilisateurConnecte;
    private LocalDateTime derniereActivite;

    private UtilisateurDAO utilisateurDAO;
    private LogActiviteDAO logDAO;

    public AuthenticationService() {
        utilisateurDAO = new UtilisateurDAO();
        logDAO = new LogActiviteDAO();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null)
            instance = new AuthenticationService();
        return instance;
    }

    public Utilisateur login(String login, String password) throws SQLException {
        Utilisateur user = utilisateurDAO.authentifier(login, password);
        if (user != null) {
            this.utilisateurConnecte = user;
            this.derniereActivite = LocalDateTime.now();
            logDAO.ajouter(LogActivite.creerLogConnexion(user.getIdUtilisateur(), user.getLogin()));
        }
        return user;
    }

    public void logout() {
        if (utilisateurConnecte != null) {
            utilisateurConnecte = null;
        }
    }

    public boolean isSessionValid() {
        if (utilisateurConnecte == null)
            return false;
        derniereActivite = LocalDateTime.now();
        return true;
    }

    public Utilisateur getCurrentUser() {
        return utilisateurConnecte;
    }
}
