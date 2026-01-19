package com.pharmacie.model;

import java.math.BigDecimal;

/**
 * Classe représentant un produit pharmaceutique.
 * Correspond à la table Produit de la base de données.
 */
public class Produit {

    private int idProduit;
    private String nom;
    private String description;
    private String codeBarre;
    private BigDecimal prixUnitaire;
    private int quantiteStock;
    private int seuilAlerte;

    /**
     * Constructeur par défaut.
     */
    public Produit() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public Produit(int idProduit, String nom, String description, String codeBarre,
                   BigDecimal prixUnitaire, int quantiteStock, int seuilAlerte) {
        this.idProduit = idProduit;
        this.nom = nom;
        this.description = description;
        this.codeBarre = codeBarre;
        this.prixUnitaire = prixUnitaire;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public Produit(String nom, String description, String codeBarre,
                   BigDecimal prixUnitaire, int quantiteStock, int seuilAlerte) {
        this.nom = nom;
        this.description = description;
        this.codeBarre = codeBarre;
        this.prixUnitaire = prixUnitaire;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
    }

    //Getters

    public int getIdProduit() {
        return idProduit;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    //Setters

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    /**
     * Vérifie si le produit est en alerte de stock.
     */
    public boolean estEnAlerte() {
        return quantiteStock <= seuilAlerte;
    }

    /**
     * Vérifie si la quantité demandée est disponible.
     */
    public boolean estDisponible(int quantiteDemandee) {
        return quantiteStock >= quantiteDemandee;
    }

    /**
     * Calcule la valeur totale du stock.
     */
    public BigDecimal calculerValeurStock() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantiteStock));
    }

    /**
     * Retourne le prix formaté.
     */
    public String getPrixFormate() {
        return String.format("%.2f TND", prixUnitaire);
    }

    @Override
    public String toString() {
        return "Produit{" +
                "idProduit=" + idProduit +
                ", nom='" + nom + '\'' +
                ", codeBarre='" + codeBarre + '\'' +
                ", prixUnitaire=" + prixUnitaire +
                ", quantiteStock=" + quantiteStock +
                ", seuilAlerte=" + seuilAlerte +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return idProduit == produit.idProduit;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idProduit);
    }
}