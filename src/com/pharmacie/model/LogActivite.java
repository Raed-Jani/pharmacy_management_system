package com.pharmacie.model;

import java.time.LocalDateTime;

public class LogActivite {

    // Constantes pour les types d'actions
    public static final String TYPE_CONNEXION = "CONNEXION";
    public static final String TYPE_VENTE = "VENTE";
    public static final String TYPE_MAJ_STOCK = "MAJ_STOCK";
    public static final String TYPE_SUPPRESSION = "SUPPRESSION";

    private int idLog;
    private LocalDateTime dateAction;
    private String typeAction;
    private String description;
    private Integer idUtilisateur; // Peut être null si l'utilisateur est supprimé

    // Attribut relationnel (non stocké en base)
    private String loginUtilisateur;

    /**
     * Constructeur par défaut.
     */
    public LogActivite() {
    }

    /**
     * Constructeur avec tous les paramètres.
     */
    public LogActivite(int idLog, LocalDateTime dateAction, String typeAction,
            String description, Integer idUtilisateur) {
        this.idLog = idLog;
        this.dateAction = dateAction;
        this.typeAction = typeAction;
        this.description = description;
        this.idUtilisateur = idUtilisateur;
    }

    /**
     * Constructeur sans ID (pour création d'un nouveau log).
     */
    public LogActivite(String typeAction, String description, Integer idUtilisateur) {
        this.dateAction = LocalDateTime.now();
        this.typeAction = typeAction;
        this.description = description;
        this.idUtilisateur = idUtilisateur;
    }

    // ========== GETTERS ==========

    public int getIdLog() {
        return idLog;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public String getTypeAction() {
        return typeAction;
    }

    public String getDescription() {
        return description;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getLoginUtilisateur() {
        return loginUtilisateur;
    }

    // ========== SETTERS ==========

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public void setTypeAction(String typeAction) {
        this.typeAction = typeAction;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public void setLoginUtilisateur(String loginUtilisateur) {
        this.loginUtilisateur = loginUtilisateur;
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Vérifie si le log est associé à un utilisateur.
     */
    public boolean aUtilisateur() {
        return idUtilisateur != null;
    }

    /**
     * Crée un log de connexion.
     */
    public static LogActivite creerLogConnexion(int idUtilisateur, String login) {
        return new LogActivite(
                TYPE_CONNEXION,
                "Connexion de l'utilisateur: " + login,
                idUtilisateur);
    }

    /**
     * Crée un log de vente.
     */
    public static LogActivite creerLogVente(int idVente, double montant, int idUtilisateur) {
        return new LogActivite(
                TYPE_VENTE,
                String.format("Vente ID %d - Montant: %.2f TND", idVente, montant),
                idUtilisateur);
    }

    /**
     * Crée un log de mise à jour de stock.
     */
    public static LogActivite creerLogMajStock(String nomProduit, int quantite, int idUtilisateur) {
        return new LogActivite(
                TYPE_MAJ_STOCK,
                String.format("Mise à jour stock: %s (Quantité: %d)", nomProduit, quantite),
                idUtilisateur);
    }

    /**
     * Crée un log de suppression.
     */
    public static LogActivite creerLogSuppression(String typeObjet, String nomObjet, int idUtilisateur) {
        return new LogActivite(
                TYPE_SUPPRESSION,
                String.format("Suppression %s: %s", typeObjet, nomObjet),
                idUtilisateur);
    }

    @Override
    public String toString() {
        return "LogActivite{" +
                "idLog=" + idLog +
                ", dateAction=" + dateAction +
                ", typeAction='" + typeAction + '\'' +
                ", description='" + description + '\'' +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LogActivite that = (LogActivite) o;
        return idLog == that.idLog;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idLog);
    }
}