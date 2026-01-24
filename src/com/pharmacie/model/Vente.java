package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Vente {

    private int idVente;
    private LocalDateTime dateVente;
    private BigDecimal totalVente;
    private Integer idClient; // Peut être null si vente sans client enregistré
    private int idUtilisateur;

    // Attributs relationnels (non stockés en base)
    private String nomClient;
    private String loginUtilisateur;

    /**
     * Constructeur par défaut.
     */
    public Vente() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public Vente(int idVente, LocalDateTime dateVente, BigDecimal totalVente,
            Integer idClient, int idUtilisateur) {
        this.idVente = idVente;
        this.dateVente = dateVente;
        this.totalVente = totalVente;
        this.idClient = idClient;
        this.idUtilisateur = idUtilisateur;
    }

    /**
     * Constructeur sans ID (pour création).
     */
    public Vente(BigDecimal totalVente, Integer idClient, int idUtilisateur) {
        this.dateVente = LocalDateTime.now();
        this.totalVente = totalVente;
        this.idClient = idClient;
        this.idUtilisateur = idUtilisateur;
    }

    // ========== GETTERS ==========

    public int getIdVente() {
        return idVente;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public BigDecimal getTotalVente() {
        return totalVente;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNomClient() {
        return nomClient;
    }

    public String getLoginUtilisateur() {
        return loginUtilisateur;
    }

    // ========== SETTERS ==========

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public void setTotalVente(BigDecimal totalVente) {
        this.totalVente = totalVente;
    }

    public void setIdClient(Integer idClient) {
        this.idClient = idClient;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public void setLoginUtilisateur(String loginUtilisateur) {
        this.loginUtilisateur = loginUtilisateur;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Vérifie si la vente est associée à un client.
     */
    public boolean aClient() {
        return idClient != null;
    }

    /**
     * Ajoute un montant au total de la vente.
     */
    public void ajouterMontant(BigDecimal montant) {
        this.totalVente = this.totalVente.add(montant);
    }

    /**
     * Retourne le total formaté avec 2 décimales.
     */
    public String getTotalFormate() {
        return String.format("%.2f TND", totalVente);
    }

    /**
     * Retourne la date formatée.
     */
    public String getDateFormatee() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateVente.format(formatter);
    }

    @Override
    public String toString() {
        return "Vente{" +
                "idVente=" + idVente +
                ", dateVente=" + dateVente +
                ", totalVente=" + totalVente +
                ", idClient=" + idClient +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vente vente = (Vente) o;
        return idVente == vente.idVente;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idVente);
    }
}