package com.pharmacie.exception;

/**
 * Exception levée lorsqu'un produit est introuvable.
 */
public class ProduitIntrouvableException extends Exception {

    private int idProduit;
    private String critereRecherche;
    private String valeurRecherche;

    public ProduitIntrouvableException(int idProduit) {
        super("Produit introuvable avec l'ID: " + idProduit);
        this.idProduit = idProduit;
        this.critereRecherche = "ID";
        this.valeurRecherche = String.valueOf(idProduit);
    }

    public ProduitIntrouvableException(String critere, String valeur) {
        super(String.format("Produit introuvable avec %s = '%s'", critere, valeur));
        this.critereRecherche = critere;
        this.valeurRecherche = valeur;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public String getCritereRecherche() {
        return critereRecherche;
    }

    public String getValeurRecherche() {
        return valeurRecherche;
    }
}
