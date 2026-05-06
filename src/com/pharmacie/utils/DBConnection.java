package com.pharmacie.utils;

import com.pharmacie.exception.ConnexionEchoueeException;
import java.sql.*;

public class DBConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pharmacie_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD"; // TODO: Replace with your MySQL root password
    private static final int DB_PORT = 3306;

    private static DBConnection instance;
    private Connection adminConnection;

    private DBConnection() throws ConnexionEchoueeException {
        initializeDriver();
    }

    public static synchronized DBConnection getInstance() throws ConnexionEchoueeException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public static Connection getConnection() throws ConnexionEchoueeException {
        return getInstance().getAdminConnection();
    }

    private void initializeDriver() throws ConnexionEchoueeException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ConnexionEchoueeException(
                    "MySQL Driver not found",
                    "Please ensure the mysql-connector-j-8.0.33.jar is in the classpath.");
        }
    }

    public Connection getAdminConnection() throws ConnexionEchoueeException {
        try {
            if (adminConnection == null || adminConnection.isClosed()) {
                adminConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                verifyAndPatchSchema(adminConnection);
            }
            return adminConnection;
        } catch (SQLException e) {
            throw new ConnexionEchoueeException(e.getMessage(), getHelpMessage(e.getMessage()));
        }
    }

    private String getHelpMessage(String message) {
        if (message.contains("Access denied")) {
            return "Incorrect MySQL credentials. Check DB_USER and DB_PASSWORD.";
        } else if (message.contains("Unknown database")) {
            return "Database 'pharmacie_db' is missing. Run the SQL script.";
        } else if (message.contains("Communications link failure")) {
            return "MySQL service is not running or port " + DB_PORT + " is blocked.";
        }
        return "Check the application logs for more details.";
    }

    private void verifyAndPatchSchema(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            patchUtilisateur(conn, meta);
            patchProduit(conn, meta);
        } catch (Exception e) {
            System.err.println("Schema verification failed: " + e.getMessage());
        }
    }

    private void patchUtilisateur(Connection conn, DatabaseMetaData meta) throws SQLException {
        addColumnIfMissing(conn, meta, "utilisateur", "nom", "VARCHAR(100)");
        addColumnIfMissing(conn, meta, "utilisateur", "prenom", "VARCHAR(100)");
    }

    private void patchProduit(Connection conn, DatabaseMetaData meta) throws SQLException {
        addColumnIfMissing(conn, meta, "Produit", "date_expiration", "DATE");
        if (addColumnIfMissing(conn, meta, "Produit", "id_fournisseur", "INT")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                        "ALTER TABLE Produit ADD CONSTRAINT fk_produit_fournisseur FOREIGN KEY (id_fournisseur) REFERENCES Fournisseur(id_fournisseur)");
            } catch (SQLException ignored) {
            }
        }
    }

    private boolean addColumnIfMissing(Connection conn, DatabaseMetaData meta, String tableName, String columnName,
            String columnType) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
                    return true;
                }
            }
        }
        return false;
    }

    public void closeAllConnections() throws ConnexionEchoueeException {
        try {
            if (adminConnection != null && !adminConnection.isClosed()) {
                adminConnection.close();
            }
        } catch (SQLException e) {
            throw new ConnexionEchoueeException("Error closing connection", e.getMessage());
        }
    }
}
