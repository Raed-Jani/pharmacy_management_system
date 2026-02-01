package com.pharmacie.model;

public class Client {

    private int idClient;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String historiqueMedical;

    /**
     * Constructeur par défaut.
     */
    public Client() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public Client(int idClient, String nom, String prenom, String telephone,
            String email, String historiqueMedical) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.historiqueMedical = historiqueMedical;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public Client(String nom, String prenom, String telephone,
            String email, String historiqueMedical) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.historiqueMedical = historiqueMedical;
    }

    // ========== GETTERS ==========

    public int getIdClient() {
        return idClient;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getHistoriqueMedical() {
        return historiqueMedical;
    }

    // ========== SETTERS ==========

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHistoriqueMedical(String historiqueMedical) {
        this.historiqueMedical = historiqueMedical;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Retourne le nom complet du client.
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Vérifie si le client a un historique médical.
     */
    public boolean aHistoriqueMedical() {
        return historiqueMedical != null && !historiqueMedical.trim().isEmpty();
    }

    /**
     * Vérifie si le client a un email.
     */
    public boolean aEmail() {
        return email != null && !email.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Client client = (Client) o;
        return idClient == client.idClient;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idClient);
    }
}
