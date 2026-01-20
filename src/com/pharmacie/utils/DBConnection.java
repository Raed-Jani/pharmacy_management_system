package com.pharmacie.utils;

import com.pharmacie.exception.ConnexionEchoueeException;
import java.sql.*;

/**
 * Classe pour gérer la connexion à la base de données MySQL.
 * ⚠️ MODIFIER LES IDENTIFIANTS SI NÉCESSAIRE ⚠️
 */
public class DBConnection {

    // ========== CONFIGURATION - À MODIFIER SELON VOS PARAMÈTRES ==========

    // URL de connexion MySQL (format: jdbc:mysql://adresse: port/base_de_donnees)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pharmacie_db";

    // Utilisateur MySQL
    // Options:
    // - "root" (si vous utilisez l'utilisateur root)
    // - "admin_pharmacie" (si vous avez exécuté le script SQL)
    private static final String DB_USER = "root";

    // Mot de passe MySQL
    // ⚠️ À CHANGER SELON VOTRE CONFIGURATION ⚠️
    private static final String DB_PASSWORD = "95551440";

    // Port MySQL (par défaut: 3306)
    private static final int DB_PORT = 3306;

    // ========================================================================

    private static DBConnection instance;
    private Connection adminConnection;

    /**
     * Constructeur privé (Singleton)
     */
    private DBConnection() throws ConnexionEchoueeException {
        initializeDriver();
    }

    /**
     * Retourne l'instance unique de DBConnection (Singleton)
     */
    public static synchronized DBConnection getInstance() throws ConnexionEchoueeException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Helper statique pour obtenir une connexion directement.
     */
    public static Connection getConnection() throws ConnexionEchoueeException {
        return getInstance().getAdminConnection();
    }

    /**
     * Initialise le driver MySQL
     */
    private void initializeDriver() throws ConnexionEchoueeException {
        System.out.println("\n========== INITIALISATION DBConnection ==========");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Driver MySQL chargé:  com.mysql.cj.jdbc. Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ ERREUR: Driver MySQL introuvable!");
            System.err.println("\n┌─────────────────────────────────────────────────────┐");
            System.err.println("│  SOLUTION:                                            │");
            System.err.println("│  1. Téléchargez mysql-connector-java-8.0.33.jar    │");
            System.err.println("│     depuis:  https://dev.mysql.com/downloads/        │");
            System.err.println("│                                                     │");
            System.err.println("│  2. Dans IntelliJ:                                    │");
            System.err.println("│     File → Project Structure → Libraries            │");
            System.err.println("│     → + → Java → Sélectionnez le JAR               │");
            System.err.println("│                                                     │");
            System.err.println("│  3. Redémarrez l'application                        │");
            System.err.println("└─────────────────────────────────────────────────────┘\n");

            throw new ConnexionEchoueeException(
                    "Driver MySQL non trouvé",
                    "Ajoutez mysql-connector-java. jar au classpath du projet");
        }
    }

    /**
     * Retourne une connexion administrateur
     */
    public Connection getAdminConnection() throws ConnexionEchoueeException {
        try {
            if (adminConnection == null || adminConnection.isClosed()) {
                System.out.println("\n→ Tentative de connexion à MySQL.. .");
                System.out.println("  URL: " + DB_URL);
                System.out.println("  Utilisateur: " + DB_USER);
                System.out.println("  Port: " + DB_PORT);

                adminConnection = DriverManager.getConnection(
                        DB_URL,
                        DB_USER,
                        DB_PASSWORD);

                System.out.println("✓ Connexion établie avec succès!");
                System.out.println("✓ Base de données: pharmacie_db");
                System.out.println("✓ Utilisateur: " + DB_USER);

                // Auto-patch schema
                verifyAndPatchSchema(adminConnection);
            }
            return adminConnection;
        } catch (SQLException e) {
            String message = e.getMessage();
            String aide = "";

            // Identifier le type d'erreur
            if (message.contains("Access denied")) {
                aide = "❌ Identifiants MySQL incorrects!\n\n" +
                        "Solutions:\n" +
                        "1. Vérifiez DB_USER:  " + DB_USER + "\n" +
                        "2. Vérifiez DB_PASSWORD (ligne 25)\n" +
                        "3. Essayez avec 'root':  DB_USER = \"root\"\n" +
                        "4. Exécutez: mysql -u root -p < database/pharmacie_db.sql";
            } else if (message.contains("Unknown database")) {
                aide = "❌ Base de données 'pharmacie_db' n'existe pas!\n\n" +
                        "Solutions:\n" +
                        "1. Exécutez le script SQL:\n" +
                        "   mysql -u root -p < database/pharmacie_db.sql\n" +
                        "2. Vérifiez le nom de la base: pharmacie_db";
            } else if (message.contains("Communications link failure") ||
                    message.contains("Connection refused")) {
                aide = "❌ MySQL n'est pas démarré ou n'est pas accessible!\n\n" +
                        "Solutions:\n" +
                        "1. Démarrez MySQL:\n" +
                        "   • Windows: net start MySQL80\n" +
                        "   • Linux:  sudo systemctl start mysql\n" +
                        "   • macOS: sudo /usr/local/mysql/support-files/mysql.server start\n" +
                        "2. Vérifiez le port:  " + DB_PORT + "\n" +
                        "3. Vérifiez l'adresse:  localhost";
            } else {
                aide = "Erreur MySQL: " + message;
            }

            System.err.println("\n✗ ERREUR DE CONNEXION");
            System.err.println("   " + message);
            System.err.println("   Aide: " + aide);

            throw new ConnexionEchoueeException(message, aide);
        }
    }

    private void verifyAndPatchSchema(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();

            // Check 'nom'
            ResultSet rs = meta.getColumns(null, null, "utilisateur", "nom");
            if (!rs.next()) {
                System.out.println("⚠️ Colonne 'nom' manquante dans Utilisateur. Tentative de correction...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Utilisateur ADD COLUMN nom VARCHAR(100)");
                    System.out.println("✓ Colonne 'nom' ajoutée.");
                } catch (SQLException ex) {
                    System.err.println("Echec ajout nom: " + ex.getMessage());
                }
            }
            rs.close();

            // Check 'prenom'
            rs = meta.getColumns(null, null, "utilisateur", "prenom");
            if (!rs.next()) {
                System.out.println("⚠️ Colonne 'prenom' manquante dans Utilisateur. Tentative de correction...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Utilisateur ADD COLUMN prenom VARCHAR(100)");
                    System.out.println("✓ Colonne 'prenom' ajoutée.");
                } catch (SQLException ex) {
                    System.err.println("Echec ajout prenom: " + ex.getMessage());
                }
            }
            rs.close();

            // --- NOUVELLES COLONNES POUR PRODUIT ---

            // Check 'date_expiration'
            rs = meta.getColumns(null, null, "Produit", "date_expiration");
            if (!rs.next()) {
                System.out.println("⚠️ Colonne 'date_expiration' manquante dans Produit. Tentative de correction...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Produit ADD COLUMN date_expiration DATE");
                    System.out.println("✓ Colonne 'date_expiration' ajoutée.");
                } catch (SQLException ex) {
                    System.err.println("Echec ajout date_expiration: " + ex.getMessage());
                }
            }
            rs.close();

            // Check 'id_fournisseur'
            rs = meta.getColumns(null, null, "Produit", "id_fournisseur");
            if (!rs.next()) {
                System.out.println("⚠️ Colonne 'id_fournisseur' manquante dans Produit. Tentative de correction...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Produit ADD COLUMN id_fournisseur INT");
                    stmt.execute(
                            "ALTER TABLE Produit ADD CONSTRAINT fk_produit_fournisseur FOREIGN KEY (id_fournisseur) REFERENCES Fournisseur(id_fournisseur)");
                    System.out.println("✓ Colonne 'id_fournisseur' et contrainte FK ajoutées.");
                } catch (SQLException ex) {
                    System.err.println("Echec ajout id_fournisseur: " + ex.getMessage());
                }
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Erreur vérification schéma: " + e.getMessage());
        }
    }

    /**
     * Ferme toutes les connexions
     */
    public void closeAllConnections() throws ConnexionEchoueeException {
        try {
            if (adminConnection != null && !adminConnection.isClosed()) {
                adminConnection.close();
                System.out.println("✓ Connexion fermée");
            }
        } catch (SQLException e) {
            throw new ConnexionEchoueeException(
                    "Erreur lors de la fermeture",
                    e.getMessage());
        }
    }
}