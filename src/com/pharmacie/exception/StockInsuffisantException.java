// ========== StockInsuffisantException.java ==========
package com.pharmacie.exception;

/**
 * Exception levée lorsque le stock d'un produit est insuffisant.
 */
public class StockInsuffisantException extends Exception {

    private String nomProduit;
    private int quantiteDemandee;
    private int quantiteDisponible;

    public StockInsuffisantException(String nomProduit, int quantiteDemandee,
                                     int quantiteDisponible) {
        super(String.format(
                "Stock insuffisant pour le produit '%s': %d demandé(s), %d disponible(s)",
                nomProduit, quantiteDemandee, quantiteDisponible
        ));
        this.nomProduit = nomProduit;
        this.quantiteDemandee = quantiteDemandee;
        this.quantiteDisponible = quantiteDisponible;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public int getManquant() {
        return quantiteDemandee - quantiteDisponible;
    }
}
