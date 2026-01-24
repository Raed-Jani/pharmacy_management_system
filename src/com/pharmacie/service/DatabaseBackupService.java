package com.pharmacie.service;

import com.pharmacie.utils.DBConnection;
import com.pharmacie.exception.ConnexionEchoueeException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.io.PrintWriter;
import java.io.FileWriter;

/**
 * Service gérant la sauvegarde manuelle de la base de données.
 * Génère un script SQL complet (INSERTs) pour la récupération.
 */
public class DatabaseBackupService {

    /**
     * Exporte toutes les tables de l'application vers un fichier SQL.
     */
    public void sauvegarder(Path destination) throws SQLException, ConnexionEchoueeException, IOException {
        Connection conn = DBConnection.getInstance().getAdminConnection();
        try (PrintWriter writer = new PrintWriter(new FileWriter(destination.toFile()))) {
            writer.println("-- Backup Pharmacie DB");
            writer.println("-- Date: " + java.time.LocalDateTime.now());
            writer.println();

            String[] tables = { "Utilisateur", "Client", "Fournisseur", "Produit", "Vente", "LigneVente",
                    "CommandeFournisseur", "LigneCommande", "LogActivite" };

            for (String table : tables) {
                writer.println("-- Table: " + table);
                exportTable(conn, table, writer);
                writer.println();
            }
        }
    }

    /**
     * Parcourt une table et génère les instructions INSERT correspondantes.
     * Gère l'échappement des caractères spéciaux pour éviter les erreurs SQL.
     */
    private void exportTable(Connection conn, String table, PrintWriter writer) throws SQLException {
        String sql = "SELECT * FROM " + table;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                writer.print("INSERT INTO " + table + " VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        writer.print("NULL");
                    } else if (value instanceof String || value instanceof java.sql.Date
                            || value instanceof java.sql.Timestamp) {
                        writer.print("'" + value.toString().replace("'", "''") + "'");
                    } else {
                        writer.print(value);
                    }
                    if (i < columnCount)
                        writer.print(", ");
                }
                writer.println(");");
            }
        }
    }
}
