package com.pharmacie.model;

import java.time.LocalDateTime;

public class CommandeFournisseur {

    // Constantes pour les statuts
    public static final String STATUT_EN_COURS = "EN_COURS";
    public static final String STATUT_RECUE = "RECUE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    private int idCommande;
    private LocalDateTime dateCreation;
    private LocalDateTime dateReception;
    private String statut;
    private int idFournisseur;

    // Attribut relationnel (non stocké en base)
    private String nomFournisseur;

    /**
     * Constructeur par défaut.
     */
    public CommandeFournisseur() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public CommandeFournisseur(int idCommande, LocalDateTime dateCreation,
            LocalDateTime dateReception, String statut, int idFournisseur) {
        this.idCommande = idCommande;
        this.dateCreation = dateCreation;
        this.dateReception = dateReception;
        this.statut = statut;
        this.idFournisseur = idFournisseur;
    }

    /**
     * Constructeur sans ID (pour création d'une nouvelle commande).
     */
    public CommandeFournisseur(String statut, int idFournisseur) {
        this.dateCreation = LocalDateTime.now();
        this.statut = statut;
        this.idFournisseur = idFournisseur;
    }

    // ========== GETTERS ==========

    public int getIdCommande() {
        return idCommande;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateReception() {
        return dateReception;
    }

    public String getStatut() {
        return statut;
    }

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    // ========== SETTERS ==========

    public void setIdCommande(int idCommande) {
        this.idCommande = idCommande;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setDateReception(LocalDateTime dateReception) {
        this.dateReception = dateReception;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public void setNomFournisseur(String nomFournisseur) {
        this.nomFournisseur = nomFournisseur;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Vérifie si la commande est en cours.
     */
    public boolean estEnCours() {
        return STATUT_EN_COURS.equals(statut);
    }

    /**
     * Vérifie si la commande a été reçue.
     */
    public boolean estRecue() {
        return STATUT_RECUE.equals(statut);
    }

    /**
     * Vérifie si la commande a été annulée.
     */
    public boolean estAnnulee() {
        return STATUT_ANNULEE.equals(statut);
    }

    /**
     * Marque la commande comme reçue.
     */
    public void marquerCommeRecue() {
        this.statut = STATUT_RECUE;
        this.dateReception = LocalDateTime.now();
    }

    /**
     * Annule la commande.
     */
    public void annuler() {
        this.statut = STATUT_ANNULEE;
    }

    /**
     * Vérifie si le statut est valide.
     */
    public boolean statutValide() {
        return STATUT_EN_COURS.equals(statut) ||
                STATUT_RECUE.equals(statut) ||
                STATUT_ANNULEE.equals(statut);
    }

    @Override
    public String toString() {
        return "CommandeFournisseur{" +
                "idCommande=" + idCommande +
                ", dateCreation=" + dateCreation +
                ", dateReception=" + dateReception +
                ", statut='" + statut + '\'' +
                ", idFournisseur=" + idFournisseur +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CommandeFournisseur that = (CommandeFournisseur) o;
        return idCommande == that.idCommande;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCommande);
    }
}
