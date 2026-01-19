package com.pharmacie.model;

import java.math.BigDecimal;

/**
 * Classe représentant une ligne de commande fournisseur.
 * Correspond à la table LigneCommande de la base de données.
 */
public class LigneCommande {

    private int idLigneCmd;
    private int idCommande;
    private int idProduit;
    private int quantiteCommandee;
    private BigDecimal prixAchat;

    // Attribut relationnel (non stocké en base)
    private String nomProduit;

    /**
     * Constructeur par défaut.
     */
    public LigneCommande() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public LigneCommande(int idLigneCmd, int idCommande, int idProduit,
                         int quantiteCommandee, BigDecimal prixAchat) {
        this.idLigneCmd = idLigneCmd;
        this.idCommande = idCommande;
        this.idProduit = idProduit;
        this.quantiteCommandee = quantiteCommandee;
        this.prixAchat = prixAchat;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public LigneCommande(int idCommande, int idProduit, int quantiteCommandee,
                         BigDecimal prixAchat) {
        this.idCommande = idCommande;
        this.idProduit = idProduit;
        this.quantiteCommandee = quantiteCommandee;
        this.prixAchat = prixAchat;
    }

    // ========== GETTERS ==========

    public int getIdLigneCmd() {
        return idLigneCmd;
    }

    public int getIdCommande() {
        return idCommande;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    // ========== SETTERS ==========

    public void setIdLigneCmd(int idLigneCmd) {
        this.idLigneCmd = idLigneCmd;
    }

    public void setIdCommande(int idCommande) {
        this.idCommande = idCommande;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Calcule le coût total de cette ligne de commande.
     */
    public BigDecimal calculerCoutTotal() {
        return prixAchat.multiply(BigDecimal.valueOf(quantiteCommandee));
    }

    /**
     * Retourne le coût total formaté.
     */
    public String getCoutTotalFormate() {
        return String.format("%.2f", calculerCoutTotal());
    }

    @Override
    public String toString() {
        return "LigneCommande{" +
                "idLigneCmd=" + idLigneCmd +
                ", idCommande=" + idCommande +
                ", idProduit=" + idProduit +
                ", quantiteCommandee=" + quantiteCommandee +
                ", prixAchat=" + prixAchat +
                ", coutTotal=" + calculerCoutTotal() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LigneCommande that = (LigneCommande) o;
        return idLigneCmd == that.idLigneCmd;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idLigneCmd);
    }
}