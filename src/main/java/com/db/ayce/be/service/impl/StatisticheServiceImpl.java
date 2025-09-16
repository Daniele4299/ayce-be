package com.db.ayce.be.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.ayce.be.dto.ProductSalesDto;
import com.db.ayce.be.dto.SessionDeltaDto;
import com.db.ayce.be.dto.TotaliDto;
import com.db.ayce.be.entity.CostoProdotto;
import com.db.ayce.be.entity.Impostazioni;
import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.repository.CostoProdottoRepository;
import com.db.ayce.be.repository.ImpostazioniRepository;
import com.db.ayce.be.repository.OrdineRepository;
import com.db.ayce.be.repository.ProdottoRepository;
import com.db.ayce.be.repository.SessioneRepository;
import com.db.ayce.be.service.StatisticheService;

@Service
@Transactional(readOnly = true)
public class StatisticheServiceImpl implements StatisticheService {

    @Autowired
    private OrdineRepository ordineRepo;

    @Autowired
    private SessioneRepository sessioneRepo;
    
    @Autowired
    private ProdottoRepository prodottoRepo;

    @Autowired
    private CostoProdottoRepository costoRepo;

    @Autowired
    private ImpostazioniRepository impostazioniRepo;

    private LocalDateTime[] resolveRange(String period, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) return new LocalDateTime[]{from, to};
        LocalDateTime now = LocalDateTime.now();
        String p = period == null ? "all" : period.toLowerCase();
        switch (p) {
            case "day": case "giorno":
                LocalDate today = now.toLocalDate();
                return new LocalDateTime[]{today.atStartOfDay(), today.atTime(LocalTime.MAX)};
            case "week": case "settimana":
                LocalDate startWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endWeek = startWeek.plusDays(6);
                return new LocalDateTime[]{startWeek.atStartOfDay(), endWeek.atTime(LocalTime.MAX)};
            case "month": case "mese":
                LocalDate firstMonth = now.toLocalDate().withDayOfMonth(1);
                LocalDate lastMonth = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
                return new LocalDateTime[]{firstMonth.atStartOfDay(), lastMonth.atTime(LocalTime.MAX)};
            case "year": case "anno":
                LocalDate firstYear = now.toLocalDate().withDayOfYear(1);
                LocalDate lastYear = now.toLocalDate().with(TemporalAdjusters.lastDayOfYear());
                return new LocalDateTime[]{firstYear.atStartOfDay(), lastYear.atTime(LocalTime.MAX)};
            default:
                return new LocalDateTime[]{LocalDateTime.of(1970,1,1,0,0), now.with(LocalTime.MAX)};
        }
    }

    private boolean isPranzo(Sessione s) {
        try {
            Optional<Impostazioni> impPranzo = impostazioniRepo.findById("ora_inizio_pranzo");
            Optional<Impostazioni> impCena = impostazioniRepo.findById("ora_inizio_cena");
            if (impPranzo.isPresent() && impCena.isPresent() && s.getOrarioInizio() != null) {
                int startPranzo = Integer.parseInt(impPranzo.get().getValore());
                int startCena = Integer.parseInt(impCena.get().getValore());
                int h = s.getOrarioInizio().getHour();
                return (h >= startPranzo && h < startCena);
            } else if (s.getOrarioInizio() != null) {
                int h = s.getOrarioInizio().getHour();
                return h >= 6 && h < 16;
            } else {
                return true;
            }
        } catch (Exception ex) {
            if (s.getOrarioInizio() == null) return true;
            int h = s.getOrarioInizio().getHour();
            return h >= 6 && h < 16;
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public TotaliDto calcolaTotali(String period, LocalDateTime from, LocalDateTime to) {
        LocalDateTime[] range = resolveRange(period, from, to);
        LocalDateTime start = range[0], end = range[1];

        List<Ordine> ordini = ordineRepo.findByOrarioBetweenAndSessioneIsDeletedFalse(start, end);
        List<Sessione> sessioni = sessioneRepo.findByOrarioInizioBetweenAndIsDeletedFalse(start, end);

        double totaleLordo = 0.0;
        double totaleCosti = 0.0;
        int sessioniAyce = 0;
        int sessioniCarta = 0;
        int aycePranzi = 0;
        int ayceCene = 0;

        // Calcolo lordo/costi per ordini
        for (Ordine o : ordini) {
            double prezzoUnit = o.getPrezzoUnitario() != null ? o.getPrezzoUnitario() : 0.0;
            int qty = o.getQuantita();
            totaleLordo += prezzoUnit * qty;

            Long pid = o.getProdotto() != null ? o.getProdotto().getId() : null;
            if (pid != null) {
                Optional<CostoProdotto> cp = costoRepo.findByProdottoId(pid);
                double costoUnit = cp.map(CostoProdotto::getCosto).orElse(0.0);
                totaleCosti += costoUnit * qty;
            }
        }

        // Calcolo sessioni e suddivisione pranzi/cene
        for (Sessione s : sessioni) {
            if (Boolean.TRUE.equals(s.getIsAyce())) {
                sessioniAyce++;
                if (isPranzo(s)) aycePranzi++;
                else ayceCene++;

                double quota = isPranzo(s) ? 20.0 : 30.0;
                int partecipanti = s.getNumeroPartecipanti() != null ? s.getNumeroPartecipanti() : 0;
                totaleLordo += quota * partecipanti;
            } else {
                sessioniCarta++;
            }
        }

        double netto = totaleLordo - totaleCosti;
        return new TotaliDto(round(totaleLordo), round(netto), sessioniAyce, sessioniCarta, aycePranzi, ayceCene);
    }


    @Override
    public TotaliDto calcolaTotaliPerSessioneId(Long sessioneId) {
    	Optional<Sessione> sOpt = sessioneRepo.findById(sessioneId)
    		    .filter(sess -> !Boolean.TRUE.equals(sess.getIsDeleted()));

        if (sOpt.isEmpty()) return new TotaliDto(0.0, 0.0, 0, 0, 0, 0);

        Sessione s = sOpt.get();
        List<Ordine> ordini = ordineRepo.findBySessioneId(sessioneId);

        double lordo = 0.0;
        double costi = 0.0;
        int sessioniAyce = 0;
        int sessioniCarta = 0;
        int aycePranzi = 0;
        int ayceCene = 0;

        for (Ordine o : ordini) {
            double prezzoUnit = o.getPrezzoUnitario() != null ? o.getPrezzoUnitario() : 0.0;
            int qty = o.getQuantita();
            lordo += prezzoUnit * qty;

            Long pid = o.getProdotto() != null ? o.getProdotto().getId() : null;
            if (pid != null) {
                Optional<CostoProdotto> cp = costoRepo.findByProdottoId(pid);
                double costoUnit = cp.map(CostoProdotto::getCosto).orElse(0.0);
                costi += costoUnit * qty;
            }
        }

        if (Boolean.TRUE.equals(s.getIsAyce())) {
            sessioniAyce = 1;
            if (isPranzo(s)) aycePranzi = 1;
            else ayceCene = 1;

            double quota = isPranzo(s) ? 20.0 : 30.0;
            int partecipanti = s.getNumeroPartecipanti() != null ? s.getNumeroPartecipanti() : 0;
            lordo += quota * partecipanti;
        } else {
            sessioniCarta = 1;
        }

        double netto = lordo - costi;
        return new TotaliDto(round(lordo), round(netto), sessioniAyce, sessioniCarta, aycePranzi, ayceCene);
    }



    @Override
    public List<ProductSalesDto> prodottiMenoVenduti(String period, LocalDateTime from, LocalDateTime to, int limit) {
        LocalDateTime[] range = resolveRange(period, from, to);
        return ordineRepo.findBottomProductsByPeriod(range[0], range[1], PageRequest.of(0, limit));
    }

    @Override
    public List<ProductSalesDto> prodottiPiùVenduti(String period, LocalDateTime from, LocalDateTime to, int limit) {
        LocalDateTime[] range = resolveRange(period, from, to);
        return ordineRepo.findTopProductsByPeriod(range[0], range[1], PageRequest.of(0, limit));
    }

    @Override
    public Integer contaSessioni(String period, LocalDateTime from, LocalDateTime to) {
        LocalDateTime[] r = resolveRange(period, from, to);
        return sessioneRepo.findByOrarioInizioBetweenAndIsDeletedFalse(r[0], r[1]).size();
    }

    @Override
    public SessionDeltaDto deltaSessione(Long sessioneId) {
        TotaliDto t = calcolaTotaliPerSessioneId(sessioneId);
        double lordo = t.getLordo();
        double netto = t.getNetto();
        double costi = lordo - netto;
        double profit = netto;
        return new SessionDeltaDto(sessioneId, round(lordo), round(netto), round(profit), round(costi));
    }
}
