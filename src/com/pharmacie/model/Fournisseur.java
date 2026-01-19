package com.pharmacie.model;

/**
 * Classe représentant un fournisseur de produits pharmaceutiques.
 * Correspond à la table Fournisseur de la base de données.
 */
public class Fournisseur {

    private int idFournisseur;
    private String nomSociete;
    private String adresse;
    private String telephone;
    private String email;

    /**
     * Constructeur par défaut.
     */
    public Fournisseur() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public Fournisseur(int idFournisseur, String nomSociete, String adresse,
                       String telephone, String email) {
        this.idFournisseur = idFournisseur;
        this.nomSociete = nomSociete;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public Fournisseur(String nomSociete, String adresse, String telephone, String email) {
        this.nomSociete = nomSociete;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
    }

    // ========== GETTERS ==========

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public String getNomSociete() {
        return nomSociete;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    // ========== SETTERS ==========

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public void setNomSociete(String nomSociete) {
        this.nomSociete = nomSociete;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Vérifie si le fournisseur a une adresse renseignée.
     */
    public boolean aAdresse() {
        return adresse != null && !adresse.trim().isEmpty();
    }

    /**
     * Vérifie si le fournisseur a un email.
     */
    public boolean aEmail() {
        return email != null && !email.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "idFournisseur=" + idFournisseur +
                ", nomSociete='" + nomSociete + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return idFournisseur == that.idFournisseur;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idFournisseur);
    }
}