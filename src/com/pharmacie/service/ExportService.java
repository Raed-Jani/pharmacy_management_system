package com.pharmacie.service;

import com.pharmacie.model.Produit;
import com.pharmacie.model.Vente;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private static final String CSV_SEP = ";";

    public void exporterProduits(List<Produit> produits, Path destination) throws IOException {
        try (PrintWriter writer = createWriter(destination)) {
            writer.println("ID;Nom;Description;Code Barre;Prix;Stock;Seuil Alerte;Date Expiration;Fournisseur");
            for (Produit p : produits) {
                writer.print(p.getIdProduit() + CSV_SEP);
                writer.print(escapeCsv(p.getNom()) + CSV_SEP);
                writer.print(escapeCsv(p.getDescription()) + CSV_SEP);
                writer.print(escapeCsv(p.getCodeBarre()) + CSV_SEP);
                writer.print(p.getPrixUnitaire() + CSV_SEP);
                writer.print(p.getQuantiteStock() + CSV_SEP);
                writer.print(p.getSeuilAlerte() + CSV_SEP);
                writer.print((p.getDateExpiration() != null ? p.getDateExpiration() : "") + CSV_SEP);
                writer.println(escapeCsv(p.getNomFournisseur() != null ? p.getNomFournisseur() : ""));
            }
        }
    }

    public void exporterVentes(List<Vente> ventes, Path destination) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (PrintWriter writer = createWriter(destination)) {
            writer.println("ID;Date;Montant;Client;Employé");
            for (Vente v : ventes) {
                writer.print(v.getIdVente() + CSV_SEP);
                writer.print(v.getDateVente().format(dtf) + CSV_SEP);
                writer.print(v.getTotalVente() + CSV_SEP);
                writer.print(escapeCsv(v.getNomClient() != null ? v.getNomClient() : "Passager") + CSV_SEP);
                writer.println(escapeCsv(v.getLoginUtilisateur() != null ? v.getLoginUtilisateur() : ""));
            }
        }
    }

    /**
     * Génère un fichier PDF des produits avec mise en évidence des stocks bas.
     * Utilise une orientation paysage pour inclure toutes les colonnes.
     */
    public void exporterProduitsPDF(List<Produit> produits, Path destination) throws IOException, DocumentException {
        Document doc = createDocument(destination, PageSize.A4.rotate());
        doc.open();
        addTitle(doc, "Product Report - Pharmacy");

        PdfPTable table = createTable(8);
        addHeaders(table,
                new String[] { "Nom", "Code Barre", "Prix", "Stock", "Seuil", "Exp.", "Fournisseur", "Status" });

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

            String status = p.getQuantiteStock() <= p.getSeuilAlerte() ? "LOW STOCK" : "OK";
            PdfPCell statusCell = new PdfPCell(new Paragraph(status, rowFont));
            if (status.equals("LOW STOCK"))
                statusCell.setBackgroundColor(new Color(231, 76, 60, 50));
            table.addCell(statusCell);
        }

        doc.add(table);
        doc.close();
    }

    public void exporterVentesPDF(List<Vente> ventes, Path destination) throws IOException, DocumentException {
        Document doc = createDocument(destination, PageSize.A4);
        doc.open();
        addTitle(doc, "Sales History - Pharmacy");

        PdfPTable table = createTable(5);
        addHeaders(table, new String[] { "ID", "Date", "Montant (TND)", "Client", "Staff" });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Vente v : ventes) {
            table.addCell(new Paragraph(String.valueOf(v.getIdVente()), rowFont));
            table.addCell(new Paragraph(v.getDateVente().format(dtf), rowFont));
            table.addCell(new Paragraph(v.getTotalVente().toString(), rowFont));
            table.addCell(new Paragraph(v.getNomClient() != null ? v.getNomClient() : "Passager", rowFont));
            table.addCell(new Paragraph(v.getLoginUtilisateur() != null ? v.getLoginUtilisateur() : "-", rowFont));
        }

        doc.add(table);
        double total = ventes.stream().mapToDouble(v -> v.getTotalVente().doubleValue()).sum();
        Paragraph footer = new Paragraph("\nTotal: " + String.format("%.2f", total) + " TND",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        footer.setAlignment(Paragraph.ALIGN_RIGHT);
        doc.add(footer);
        doc.close();
    }

    private Document createDocument(Path destination, com.lowagie.text.Rectangle pageSize)
            throws IOException, DocumentException {
        Document document = new Document(pageSize);
        PdfWriter.getInstance(document, new FileOutputStream(destination.toFile()));
        return document;
    }

    private void addTitle(Document doc, String titleText) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        doc.add(title);
    }

    private PdfPTable createTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        return table;
    }

    private void addHeaders(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(
                    new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    /**
     * Crée un PrintWriter avec encodage UTF-8 et BOM (Byte Order Mark).
     * Le BOM est nécessaire pour que Microsoft Excel reconnaisse l'UTF-8 à
     * l'ouverture du CSV.
     */
    private PrintWriter createWriter(Path destination) throws IOException {
        FileOutputStream fos = new FileOutputStream(destination.toFile());
        fos.write(0xEF);
        fos.write(0xBB);
        fos.write(0xBF);
        return new PrintWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
    }

    /**
     * Nettoie les chaînes de caractères pour le format CSV (gestion des guillemets
     * et séparateurs).
     */
    private String escapeCsv(String text) {
        if (text == null)
            return "";
        String escaped = text.replace("\"", "\"\"");
        if (escaped.contains(CSV_SEP) || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
