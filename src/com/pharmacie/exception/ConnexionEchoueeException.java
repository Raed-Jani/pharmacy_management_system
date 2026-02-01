package com.pharmacie.exception;

public class ConnexionEchoueeException extends Exception {

    private String messageAide;

    public ConnexionEchoueeException(String message, String messageAide) {
        super(message);
        this.messageAide = messageAide;
    }

    public String getMessageAide() {
        return messageAide;
    }

    @Override
    public String toString() {
        return super.toString() + "\nAide: " + messageAide;
    }
}
