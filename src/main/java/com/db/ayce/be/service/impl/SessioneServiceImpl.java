package com.db.ayce.be.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.db.ayce.be.dto.ResocontoDto;
import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.entity.Tavolo;
import com.db.ayce.be.repository.SessioneRepository;
import com.db.ayce.be.service.OrdineService;
import com.db.ayce.be.service.SessioneService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessioneServiceImpl implements SessioneService {

    private final SessioneRepository sessioneRepository;
   @Autowired
   OrdineService ordineService;


    @Override
    public List<Sessione> findAll() {
        return sessioneRepository.findAll();
    }

    @Override
    public Sessione findById(Long id) {
        return sessioneRepository.findById(id).orElse(null);
    }

    @Override
    public Sessione save(Sessione sessione) {
        return sessioneRepository.save(sessione);
    }

    @Override
    public Sessione update(Long id, Sessione sessione) {
        sessione.setId(id);
        return sessioneRepository.save(sessione);
    }

    @Override
    public void delete(Long id) {
        sessioneRepository.deleteById(id);
    }

    @Override
    public byte[] generatePdfResoconto(Long id) {
        Sessione sessione = sessioneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessione non trovata"));

        List<Ordine> ordineList = ordineService.findBySessione(sessione);

        // Raggruppa ordini per prodotto
        Map<Long, OrderSummary> riepilogo = ordineList.stream()
                .collect(Collectors.toMap(
                        o -> o.getProdotto().getId(),
                        o -> new OrderSummary(o.getProdotto().getNome(), o.getQuantita(), o.getPrezzoUnitario()),
                        (a, b) -> {
                            a.quantita += b.quantita;
                            return a;
                        }
                ));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Intestazione
            document.add(new Paragraph("Blackout - Tavolo " + sessione.getTavolo().getNumero() +
                    " - Sessione " + sessione.getId()));
            document.add(new Paragraph(" "));

            // Lista ordini con prezzo > 0
            PdfPTable tablePrezzo = new PdfPTable(4);
            tablePrezzo.setWidthPercentage(100);
            tablePrezzo.setWidths(new float[]{4, 1, 2, 2});

            tablePrezzo.addCell("Prodotto");
            tablePrezzo.addCell("Quantità");
            tablePrezzo.addCell("Prezzo Unitario");
            tablePrezzo.addCell("Totale");

            double totale = 0;

            for (OrderSummary summary : riepilogo.values()) {
                if (summary.prezzoUnitario > 0) {
                    double subtot = summary.quantita * summary.prezzoUnitario;
                    totale += subtot;

                    tablePrezzo.addCell(summary.nome);
                    tablePrezzo.addCell("x" + summary.quantita);
                    tablePrezzo.addCell(String.format("%.2f €", summary.prezzoUnitario));
                    tablePrezzo.addCell(String.format("%.2f €", subtot));
                }
            }

            // Quota AYCE, se attiva, va nella tabella “prezzata”
            if (Boolean.TRUE.equals(sessione.getIsAyce())) {
                int ora = sessione.getOrarioInizio().getHour();
                double prezzoAyce = (ora >= 2 && ora < 16) ? 20.0 : 30.0;
                double totaleAyce = prezzoAyce * sessione.getNumeroPartecipanti();
                totale += totaleAyce;

                tablePrezzo.addCell("Quota AYCE");
                tablePrezzo.addCell("x" + sessione.getNumeroPartecipanti());
                tablePrezzo.addCell(String.format("%.2f €", prezzoAyce));
                tablePrezzo.addCell(String.format("%.2f €", totaleAyce));
            }

            document.add(tablePrezzo);

            // Lista ordini gratuiti
            PdfPTable tableGratuita = new PdfPTable(4);
            tableGratuita.setWidthPercentage(100);
            tableGratuita.setWidths(new float[]{4, 1, 2, 2});

            for (OrderSummary summary : riepilogo.values()) {
                if (summary.prezzoUnitario == 0) {
                    tableGratuita.addCell(summary.nome);
                    tableGratuita.addCell("x" + summary.quantita);
                    tableGratuita.addCell("-");
                    tableGratuita.addCell("0,00 €");
                }
            }

            // Aggiungi tabella gratuita solo se ha righe
            if (tableGratuita.getRows().size() > 0) {
                document.add(new Paragraph(" "));
                document.add(tableGratuita);
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Totale finale: " + String.format("%.2f €", totale)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Grazie e arrivederci!"));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore nella generazione del PDF", e);
        }
    }


    // Classe helper per il riepilogo
    private static class OrderSummary {
        String nome;
        int quantita;
        double prezzoUnitario;

        OrderSummary(String nome, int quantita, double prezzoUnitario) {
            this.nome = nome;
            this.quantita = quantita;
            this.prezzoUnitario = prezzoUnitario;
        }
    }

    @Override
    public List<ResocontoDto> getResoconto(Long id) {
		Sessione sessione = sessioneRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessione non trovata"));
		List<Ordine> ordineList = ordineService.findBySessione(sessione);
		List<ResocontoDto> resoconto = new ArrayList<>();
		// Riga per ogni ordine (senza raggruppamento, in ordine di arrivo)
		for (Ordine ordine : ordineList) {
			double subtot = ordine.getQuantita() * ordine.getPrezzoUnitario();
			resoconto.add(new ResocontoDto(ordine.getId(), ordine.getProdotto().getNome(), ordine.getQuantita(),
					ordine.getPrezzoUnitario(), subtot, ordine.getOrario(), // supponendo LocalDateTime orario
					sessione.getTavolo().getNumero(), ordine.getStato() // supponendo String stato
			));
		}
		return resoconto;
    }
    
    @Override
    public Sessione findAttivaByTavolo(Tavolo tavolo) {
        return sessioneRepository.findByTavoloAndStato(tavolo, "ATTIVA").orElse(null);
    }

	@Override
	public Sessione findAttivaById(Long sessioneId) {
		return sessioneRepository.findById(sessioneId)
                .filter(sessione -> "ATTIVA".equalsIgnoreCase(sessione.getStato()))
                .orElse(null);
	}

}
