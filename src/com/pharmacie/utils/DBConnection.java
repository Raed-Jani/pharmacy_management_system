package com.pharmacie.utils;

import com.pharmacie.exception.ConnexionEchoueeException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe singleton pour gérer la connexion à la base de données MySQL.
 * VERSION SIMPLIFIÉE - Utilise l'utilisateur root pour faciliter le développement.
 *
 * IMPORTANT: Modifiez DB_PASSWORD avec votre mot de passe MySQL root !
 */
public class DBConnection {

    // ========== CONFIGURATION - À MODIFIER SELON VOTRE INSTALLATION ==========

    // URL de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pharmacie_db";

    // Paramètres de connexion
    private static final String DB_PARAMS = "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";

    // MODIFIEZ CE MOT DE PASSE avec votre mot de passe MySQL root !
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "95551440"; // ← METTEZ VOTRE MOT DE PASSE ICI

    // ========== SINGLETON ==========
    private static DBConnection instance;
    private Connection adminConnection;
    private Connection employeConnection;

    /**
     * Constructeur privé (pattern Singleton).
     */
    private DBConnection() throws ConnexionEchoueeException {
        System.out.println("\n========== INITIALISATION DBConnection ==========");
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Driver MySQL chargé: com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ ERREUR CRITIQUE: Driver MySQL introuvable !");
            System.err.println("\n┌─────────────────────────────────────────────────────┐");
            System.err.println("│  SOLUTION:                                          │");
            System.err.println("│  1. Téléchargez mysql-connector-java-8.0.33.jar    │");
            System.err.println("│     depuis: https://dev.mysql.com/downloads/        │");
            System.err.println("│                                                     │");
            System.err.println("│  2. Dans IntelliJ:                                  │");
            System.err.println("│     File → Project Structure → Libraries            │");
            System.err.println("│     → + → Java → Sélectionnez le JAR               │");
            System.err.println("│                                                     │");
            System.err.println("│  3. Dans Eclipse:                                   │");
            System.err.println("│     Build Path → Add External JARs                  │");
            System.err.println("│     → Sélectionnez le JAR                          │");
            System.err.println("└─────────────────────────────────────────────────────┘\n");

            throw new ConnexionEchoueeException(
                    "Driver MySQL non trouvé",
                    "Ajoutez mysql-connector-java.jar au classpath du projet"
            );
        }
    }

    /**
     * Récupère l'instance unique de DBConnection.
     */
    public static synchronized DBConnection getInstance() throws ConnexionEchoueeException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Récupère la connexion administrateur.
     */
    public synchronized Connection getAdminConnection() throws ConnexionEchoueeException {
        return getConnection("ADMIN");
    }

    /**
     * Récupère la connexion employé (même que admin dans cette version simplifiée).
     */
    public synchronized Connection getEmployeConnection() throws ConnexionEchoueeException {
        return getConnection("EMPLOYE");
    }

    /**
     * Méthode privée pour créer/récupérer une connexion.
     */
    private Connection getConnection(String type) throws ConnexionEchoueeException {
        try {
            Connection conn = (type.equals("ADMIN")) ? adminConnection : employeConnection;

            if (conn == null || conn.isClosed()) {
                String fullUrl = DB_URL + DB_PARAMS;

                System.out.println("\n┌─────────────────────────────────────────────────────┐");
                System.out.println("│  Connexion " + type + " à MySQL                        ");
                System.out.println("├─────────────────────────────────────────────────────┤");
                System.out.println("│  URL: " + DB_URL);
                System.out.println("│  User: " + DB_USER);
                System.out.println("└─────────────────────────────────────────────────────┘");

                conn = DriverManager.getConnection(fullUrl, DB_USER, DB_PASSWORD);

                if (type.equals("ADMIN")) {
                    adminConnection = conn;
                } else {
                    employeConnection = conn;
                }

                System.out.println("✓ Connexion " + type + " établie avec succès\n");
            }

            return conn;

        } catch (SQLException e) {
            System.err.println("\n✗ ERREUR DE CONNEXION À MYSQL !");
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());

            String messageAide = diagnostiquerErreur(e);

            System.err.println("\n" + messageAide);

            throw new ConnexionEchoueeException(
                    "Impossible de se connecter à MySQL",
                    messageAide
            );
        }
    }

    /**
     * Diagnostique le type d'erreur et fournit une aide ciblée.
     */
    private String diagnostiquerErreur(SQLException e) {
        int errorCode = e.getErrorCode();
        String message = e.getMessage().toLowerCase();

        // Erreur 1045: Access denied
        if (errorCode == 1045 || message.contains("access denied")) {
            return "┌─────────────────────────────────────────────────────┐\n" +
                    "│  ERREUR D'AUTHENTIFICATION                          │\n" +
                    "├─────────────────────────────────────────────────────┤\n" +
                    "│  Le mot de passe root est incorrect !              │\n" +
                    "│                                                     │\n" +
                    "│  SOLUTION:                                          │\n" +
                    "│  1. Ouvrez DBConnection.java                        │\n" +
                    "│  2. Modifiez la ligne 21:                           │\n" +
                    "│     private static final String DB_PASSWORD = \"\"; │\n" +
                    "│     Mettez votre mot de passe MySQL root           │\n" +
                    "│                                                     │\n" +
                    "│  3. Si vous ne connaissez pas votre mot de passe:  │\n" +
                    "│     - Réinitialisez-le via MySQL Workbench         │\n" +
                    "│     - Ou via la ligne de commande                  │\n" +
                    "└─────────────────────────────────────────────────────┘";
        }

        // Erreur 1049: Unknown database
        if (errorCode == 1049 || message.contains("unknown database")) {
            return "┌─────────────────────────────────────────────────────┐\n" +
                    "│  BASE DE DONNÉES INTROUVABLE                        │\n" +
                    "├─────────────────────────────────────────────────────┤\n" +
                    "│  La base 'pharmacie_db' n'existe pas !             │\n" +
                    "│                                                     │\n" +
                    "│  SOLUTION:                                          │\n" +
                    "│  1. Ouvrez MySQL:                                   │\n" +
                    "│     mysql -u root -p                                │\n" +
                    "│                                                     │\n" +
                    "│  2. Exécutez le script SQL:                         │\n" +
                    "│     SOURCE /chemin/vers/pharmacie_db.sql;           │\n" +
                    "│                                                     │\n" +
                    "│  3. Ou via MySQL Workbench:                         │\n" +
                    "│     File → Run SQL Script → pharmacie_db.sql       │\n" +
                    "└─────────────────────────────────────────────────────┘";
        }

        // Erreur 0 ou 2003: Connection refused
        if (errorCode == 0 || errorCode == 2003 || message.contains("connection refused")) {
            return "┌─────────────────────────────────────────────────────┐\n" +
                    "│  MYSQL N'EST PAS DÉMARRÉ                            │\n" +
                    "├─────────────────────────────────────────────────────┤\n" +
                    "│  Le serveur MySQL ne répond pas !                  │\n" +
                    "│                                                     │\n" +
                    "│  SOLUTION:                                          │\n" +
                    "│  Windows:                                           │\n" +
                    "│    1. Tapez 'services.msc' dans le menu Démarrer   │\n" +
                    "│    2. Cherchez 'MySQL'                              │\n" +
                    "│    3. Cliquez 'Démarrer'                            │\n" +
                    "│                                                     │\n" +
                    "│  Linux:                                             │\n" +
                    "│    sudo service mysql start                         │\n" +
                    "│                                                     │\n" +
                    "│  Mac:                                               │\n" +
                    "│    mysql.server start                               │\n" +
                    "└─────────────────────────────────────────────────────┘";
        }

        // Erreur générique
        return "┌─────────────────────────────────────────────────────┐\n" +
                "│  VÉRIFIEZ:                                          │\n" +
                "│  1. MySQL est démarré                               │\n" +
                "│  2. Le mot de passe root est correct                │\n" +
                "│  3. La base 'pharmacie_db' existe                   │\n" +
                "│  4. MySQL écoute sur le port 3306                   │\n" +
                "└─────────────────────────────────────────────────────┘";
    }

    /**
     * Ferme toutes les connexions.
     */
    public synchronized void closeAllConnections() {
        try {
            if (adminConnection != null && !adminConnection.isClosed()) {
                adminConnection.close();
                System.out.println("✓ Connexion admin fermée");
            }
            if (employeConnection != null && !employeConnection.isClosed()) {
                employeConnection.close();
                System.out.println("✓ Connexion employé fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
    }

    /**
     * Teste la connexion à la base de données.
     */
    public static boolean testerConnexion() {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║          TEST DE CONNEXION À LA BASE DE DONNÉES       ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            DBConnection db = getInstance();
            Connection conn = db.getAdminConnection();
            boolean isValid = conn != null && !conn.isClosed();

            if (isValid) {
                System.out.println("\n╔════════════════════════════════════════════════════════╗");
                System.out.println("║                  ✓ CONNEXION RÉUSSIE                  ║");
                System.out.println("╚════════════════════════════════════════════════════════╝\n");
            }

            return isValid;

        } catch (ConnexionEchoueeException e) {
            System.err.println("\n╔════════════════════════════════════════════════════════╗");
            System.err.println("║                  ✗ CONNEXION ÉCHOUÉE                  ║");
            System.err.println("╚════════════════════════════════════════════════════════╝");
            return false;

        } catch (SQLException e) {
            System.err.println("\n✗ Erreur SQL: " + e.getMessage());
            return false;
        }
    }
}