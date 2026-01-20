package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe représentant un employé (extension de Utilisateur).
 * Contient les informations spécifiques RH.
 */
public class Employe {

    private int idEmploye;
    private int idUtilisateur; // FK vers Utilisateur
    private String poste;
    private LocalDate dateEmbauche;
    private BigDecimal salaire;
    private String departement;

    // Objet Utilisateur associé (pour accès facile aux infos de connexion)
    private Utilisateur utilisateur;

    public Employe() {
    }

    public Employe(int idEmploye, int idUtilisateur, String poste, LocalDate dateEmbauche, BigDecimal salaire,
            String departement) {
        this.idEmploye = idEmploye;
        this.idUtilisateur = idUtilisateur;
        this.poste = poste;
        this.dateEmbauche = dateEmbauche;
        this.salaire = salaire;
        this.departement = departement;
    }

    // ========== GETTERS & SETTERS ==========

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public BigDecimal getSalaire() {
        return salaire;
    }

    public void setSalaire(BigDecimal salaire) {
        this.salaire = salaire;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        if (utilisateur != null) {
            this.idUtilisateur = utilisateur.getIdUtilisateur();
        }
    }

    // ========== DELEGATE METHODS FOR UI/TABLEVIEW ==========

    public String getNom() {
        return utilisateur != null ? utilisateur.getNom() : "";
    }

    public String getPrenom() {
        return utilisateur != null ? utilisateur.getPrenom() : "";
    }

    public String getLogin() {
        return utilisateur != null ? utilisateur.getLogin() : "";
    }

    @Override
    public String toString() {
        return "Employe{" +
                "idEmploye=" + idEmploye +
                ", idUtilisateur=" + idUtilisateur +
                ", poste='" + poste + '\'' +
                ", departement='" + departement + '\'' +
                '}';
    }
}
