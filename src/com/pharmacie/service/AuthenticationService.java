package com.pharmacie.service;

import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.dao.LogActiviteDAO;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.model.LogActivite;
import com.pharmacie.exception.ConnexionEchoueeException;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service gérant l'authentification et la session utilisateur.
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private Utilisateur utilisateurConnecte;
    private LocalDateTime derniereActivite;

    private UtilisateurDAO utilisateurDAO;
    private LogActiviteDAO logDAO;

    // Timeout de session (Désactivé)
    // private static final int SESSION_TIMEOUT_MINUTES = 30;

    public AuthenticationService() throws ConnexionEchoueeException {
        try {
            utilisateurDAO = new UtilisateurDAO();
            logDAO = new LogActiviteDAO();
        } catch (ConnexionEchoueeException e) {
            throw e;
        }
    }

    public static synchronized AuthenticationService getInstance() throws ConnexionEchoueeException {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Tente de connecter un utilisateur.
     */
    public Utilisateur login(String login, String password) throws SQLException {
        Utilisateur user = utilisateurDAO.authentifier(login, password);

        if (user != null) {
            this.utilisateurConnecte = user;
            this.derniereActivite = LocalDateTime.now();

            // Log de connexion
            logDAO.ajouter(LogActivite.creerLogConnexion(user.getIdUtilisateur(), user.getLogin()));
        }

        return user;
    }

    /**
     * Déconnecte l'utilisateur actuel.
     */
    public void logout() {
        if (utilisateurConnecte != null) {
            // Log de déconnexion (optionnel, si on veut traquer la fin)
            // logDAO.ajouter(...)
            utilisateurConnecte = null;
        }
    }

    /**
     * Vérifie si la session est active et valide.
     */
    public boolean isSessionValid() {
        if (utilisateurConnecte == null) {
            return false;
        }

        // Timeout désactivé
        derniereActivite = LocalDateTime.now();
        return true;
    }

    public Utilisateur getCurrentUser() {
        return utilisateurConnecte;
    }
}
