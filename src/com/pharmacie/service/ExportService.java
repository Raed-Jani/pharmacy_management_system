package com.pharmacie.service;

import com.pharmacie.model.Produit;
import com.pharmacie.model.Vente;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Element;
import java.awt.Color;

/**
 * Service pour exporter des données au format CSV et PDF.
 */
public class ExportService {

    private static final String CSV_SEPARATOR = ";";

    /**
     * Exporte une liste de produits en CSV.
     */
    public void exporterProduits(List<Produit> produits, Path destination) throws IOException {
        try (PrintWriter writer = createWriter(destination)) {
            // Header
            writer.println("ID;Nom;Description;Code Barre;Prix;Stock;Seuil Alerte;Date Expiration;Fournisseur");

            for (Produit p : produits) {
                writer.print(p.getIdProduit() + CSV_SEPARATOR);
                writer.print(escapeCsv(p.getNom()) + CSV_SEPARATOR);
                writer.print(escapeCsv(p.getDescription()) + CSV_SEPARATOR);
                writer.print(escapeCsv(p.getCodeBarre()) + CSV_SEPARATOR);
                writer.print(p.getPrixUnitaire() + CSV_SEPARATOR);
                writer.print(p.getQuantiteStock() + CSV_SEPARATOR);
                writer.print(p.getSeuilAlerte() + CSV_SEPARATOR);
                writer.print((p.getDateExpiration() != null ? p.getDateExpiration() : "") + CSV_SEPARATOR);
                writer.println(escapeCsv(p.getNomFournisseur() != null ? p.getNomFournisseur() : ""));
            }
        }
    }

    /**
     * Exporte une liste de ventes en CSV.
     */
    public void exporterVentes(List<Vente> ventes, Path destination) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (PrintWriter writer = createWriter(destination)) {
            // Header
            writer.println("ID;Date;Montant;Client;Employé");

            for (Vente v : ventes) {
                writer.print(v.getIdVente() + CSV_SEPARATOR);
                writer.print(v.getDateVente().format(dtf) + CSV_SEPARATOR);
                writer.print(v.getTotalVente() + CSV_SEPARATOR);
                writer.print(
                        escapeCsv(v.getNomClient() != null ? v.getNomClient() : "Client de passage") + CSV_SEPARATOR);
                writer.println(escapeCsv(v.getLoginUtilisateur() != null ? v.getLoginUtilisateur() : ""));
            }
        }
    }

    /**
     * Exporte une liste de produits en PDF.
     */
    public void exporterProduitsPDF(List<Produit> produits, Path destination) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(destination.toFile()));

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph("Rapport des Produits - Pharmacie", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Header
        String[] headers = { "Nom", "Code Barre", "Prix", "Stock", "Seuil", "Exp.", "Fournisseur", "Alerte" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(
                    new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Produit p : produits) {
            table.addCell(new Paragraph(p.getNom(), rowFont));
            table.addCell(new Paragraph(p.getCodeBarre() != null ? p.getCodeBarre() : "", rowFont));
            table.addCell(new Paragraph(p.getPrixUnitaire().toString(), rowFont));
            table.addCell(new Paragraph(String.valueOf(p.getQuantiteStock()), rowFont));
            table.addCell(new Paragraph(String.valueOf(p.getSeuilAlerte()), rowFont));
            table.addCell(
                    new Paragraph(p.getDateExpiration() != null ? p.getDateExpiration().toString() : "-", rowFont));
            table.addCell(new Paragraph(p.getNomFournisseur() != null ? p.getNomFournisseur() : "-", rowFont));

            String status = p.getQuantiteStock() <= p.getSeuilAlerte() ? "STOCK BAS" : "OK";
            PdfPCell statusCell = new PdfPCell(new Paragraph(status, rowFont));
            if (status.equals("STOCK BAS"))
                statusCell.setBackgroundColor(new Color(231, 76, 60, 50));
            table.addCell(statusCell);
        }

        document.add(table);
        document.close();
    }

    /**
     * Exporte une liste de ventes en PDF.
     */
    public void exporterVentesPDF(List<Vente> ventes, Path destination) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(destination.toFile()));

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph("Historique des Ventes - Pharmacie", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        // Header
        String[] headers = { "ID", "Date", "Montant (TND)", "Client", "Vendeur" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(
                    new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Vente v : ventes) {
            table.addCell(new Paragraph(String.valueOf(v.getIdVente()), rowFont));
            table.addCell(new Paragraph(v.getDateVente().format(dtf), rowFont));
            table.addCell(new Paragraph(v.getTotalVente().toString(), rowFont));
            table.addCell(new Paragraph(v.getNomClient() != null ? v.getNomClient() : "Passager", rowFont));
            table.addCell(new Paragraph(v.getLoginUtilisateur() != null ? v.getLoginUtilisateur() : "-", rowFont));
        }

        document.add(table);

        // Total sum
        double total = ventes.stream().mapToDouble(v -> v.getTotalVente().doubleValue()).sum();
        Paragraph footer = new Paragraph("\nTotal Général: " + String.format("%.2f", total) + " TND",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        footer.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(footer);

        document.close();
    }

    /**
     * Crée un PrintWriter avec l'encodage UTF-8 et ajoute le BOM pour Excel.
     */
    private PrintWriter createWriter(Path destination) throws IOException {
        FileOutputStream fos = new FileOutputStream(destination.toFile());
        // Write UTF-8 BOM
        fos.write(0xEF);
        fos.write(0xBB);
        fos.write(0xBF);
        return new PrintWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
    }

    /**
     * Échappe un champ CSV selon la RFC 4180.
     */
    private String escapeCsv(String text) {
        if (text == null)
            return "";

        String escaped = text.replace("\"", "\"\""); // Double quotes
        if (escaped.contains(CSV_SEPARATOR) || escaped.contains("\"") || escaped.contains("\n")
                || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
