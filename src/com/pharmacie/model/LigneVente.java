package com.pharmacie.model;

import java.math.BigDecimal;

public class LigneVente {

    private int idLigneVente;
    private int idVente;
    private int idProduit;
    private int quantiteVendue;
    private BigDecimal prixApplique;

    // Attribut relationnel (non stocké en base)
    private String nomProduit;

    /**
     * Constructeur par défaut.
     */
    public LigneVente() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public LigneVente(int idLigneVente, int idVente, int idProduit,
            int quantiteVendue, BigDecimal prixApplique) {
        this.idLigneVente = idLigneVente;
        this.idVente = idVente;
        this.idProduit = idProduit;
        this.quantiteVendue = quantiteVendue;
        this.prixApplique = prixApplique;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public LigneVente(int idVente, int idProduit, int quantiteVendue,
            BigDecimal prixApplique) {
        this.idVente = idVente;
        this.idProduit = idProduit;
        this.quantiteVendue = quantiteVendue;
        this.prixApplique = prixApplique;
    }

    // ========== GETTERS ==========

    public int getIdLigneVente() {
        return idLigneVente;
    }

    public int getIdVente() {
        return idVente;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public int getQuantiteVendue() {
        return quantiteVendue;
    }

    public BigDecimal getPrixApplique() {
        return prixApplique;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    // ========== SETTERS ==========

    public void setIdLigneVente(int idLigneVente) {
        this.idLigneVente = idLigneVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setQuantiteVendue(int quantiteVendue) {
        this.quantiteVendue = quantiteVendue;
    }

    public void setPrixApplique(BigDecimal prixApplique) {
        this.prixApplique = prixApplique;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Calcule le montant total de cette ligne de vente.
     */
    public BigDecimal calculerMontantTotal() {
        return prixApplique.multiply(BigDecimal.valueOf(quantiteVendue));
    }

    /**
     * Retourne le montant total formaté.
     */
    public String getMontantTotalFormate() {
        return String.format("%.2f", calculerMontantTotal());
    }

    @Override
    public String toString() {
        return "LigneVente{" +
                "idLigneVente=" + idLigneVente +
                ", idVente=" + idVente +
                ", idProduit=" + idProduit +
                ", quantiteVendue=" + quantiteVendue +
                ", prixApplique=" + prixApplique +
                ", montantTotal=" + calculerMontantTotal() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LigneVente that = (LigneVente) o;
        return idLigneVente == that.idLigneVente;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idLigneVente);
    }
}
