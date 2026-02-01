package com.pharmacie.model;

public class Utilisateur {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYE = "EMPLOYE";

    private int idUtilisateur;
    private String login;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String role;

    /**
     * Constructeur par défaut.
     */
    public Utilisateur() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public Utilisateur(int idUtilisateur, String login, String motDePasse, String nom, String prenom, String role) {
        this.idUtilisateur = idUtilisateur;
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public Utilisateur(String login, String motDePasse, String role) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // ========== GETTERS ==========

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getLogin() {
        return login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getRole() {
        return role;
    }

    // ========== SETTERS ==========

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Vérifie si l'utilisateur est un administrateur.
     */
    public boolean estAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /**
     * Vérifie si l'utilisateur est un employé.
     */
    public boolean estEmploye() {
        return ROLE_EMPLOYE.equals(role);
    }

    /**
     * Vérifie si le rôle est valide.
     */
    public boolean roleValide() {
        return ROLE_ADMIN.equals(role) || ROLE_EMPLOYE.equals(role);
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", login='" + login + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Utilisateur that = (Utilisateur) o;
        return idUtilisateur == that.idUtilisateur;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idUtilisateur);
    }
}
