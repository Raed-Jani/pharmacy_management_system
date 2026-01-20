package com.pharmacie.model;

import java.math.BigDecimal;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LigneCommandeFournisseur {
    private int idLigneCmd;
    private int idCommande;
    private int idProduit;
    private int quantiteCommandee;
    private BigDecimal prixAchat;

    // Champs non persistés pour l'affichage
    private String nomProduit;
    private String codeBarre;

    public LigneCommandeFournisseur() {
    }

    public LigneCommandeFournisseur(int idProduit, String nomProduit, int quantiteCommandee, BigDecimal prixAchat) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.quantiteCommandee = quantiteCommandee;
        this.prixAchat = prixAchat;
    }

    // Getters et Setters
    public int getIdLigneCmd() {
        return idLigneCmd;
    }

    public void setIdLigneCmd(int idLigneCmd) {
        this.idLigneCmd = idLigneCmd;
    }

    public int getIdCommande() {
        return idCommande;
    }

    public void setIdCommande(int idCommande) {
        this.idCommande = idCommande;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public BigDecimal getTotaleLigne() {
        if (prixAchat == null)
            return BigDecimal.ZERO;
        return prixAchat.multiply(BigDecimal.valueOf(quantiteCommandee));
    }
}
