package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.Payment;
import com.example.fitnessBackEnd.entity.Subscription;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptPdfService {

    private static final Color DARK = new Color(11, 18, 32), ORANGE = new Color(249, 115, 22),
            MUTED = new Color(100, 116, 139), LINE = new Color(226, 232, 240);
    private static final com.lowagie.text.Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, DARK);
    private static final com.lowagie.text.Font SUB = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
    private static final com.lowagie.text.Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, DARK);
    private static final com.lowagie.text.Font BIG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, ORANGE);

    @Value("${app.currency:MAD}") private String currency;

    public byte[] generate(Payment p) {
        Subscription s = p.getSubscription();
        DateTimeFormatter d = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("FITFORGE", TITLE));
            doc.add(new Paragraph("Reçu de paiement  |  N° " + p.getReceiptNumber(), SUB));
            doc.add(Chunk.NEWLINE);

            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(100);
            row(t, "Membre", s.getMember().getFirstName() + " " + s.getMember().getLastName());
            row(t, "Abonnement", s.getPlan().getName());
            row(t, "Période", s.getStartDate().format(d) + " au " + s.getEndDate().format(d));
            row(t, "Mode de paiement", p.getMethod().name());
            row(t, "Date de paiement", p.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            doc.add(t);

            doc.add(Chunk.NEWLINE);
            Paragraph amount = new Paragraph("TOTAL : " + p.getAmount().setScale(2, RoundingMode.HALF_UP)
                    .toPlainString() + " " + currency, BIG);
            amount.setAlignment(Element.ALIGN_RIGHT);
            doc.add(amount);

            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Merci de votre confiance. Conservez ce reçu.", SUB));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le reçu", e);
        }
    }

    private void row(PdfPTable t, String label, String value) {
        PdfPCell a = new PdfPCell(new Phrase(label, SUB)), b = new PdfPCell(new Phrase(value, BOLD));
        for (PdfPCell c : new PdfPCell[]{a, b}) { c.setBorder(PdfPCell.BOTTOM); c.setBorderColor(LINE); c.setPadding(8); }
        t.addCell(a); t.addCell(b);
    }
}
