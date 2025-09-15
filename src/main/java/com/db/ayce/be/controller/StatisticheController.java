package com.db.ayce.be.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.ayce.be.dto.ProductSalesDto;
import com.db.ayce.be.dto.SessionDeltaDto;
import com.db.ayce.be.dto.TotaliDto;
import com.db.ayce.be.service.StatisticheService;

@RestController
@RequestMapping("/api/stats")
public class StatisticheController {

    @Autowired
    private StatisticheService statsService;

    // Totali (lordo/netto/sessioni)
    @GetMapping("/totali")
    public ResponseEntity<TotaliDto> getTotali(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TotaliDto dto = statsService.calcolaTotali(period, from, to);
        return ResponseEntity.ok(dto);
    }

    // Conta sessioni
    @GetMapping("/sessioni/count")
    public ResponseEntity<Integer> contaSessioni(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Integer count = statsService.contaSessioni(period, from, to);
        return ResponseEntity.ok(count);
    }

    // Prodotti più venduti
    @GetMapping("/prodotti/top")
    public ResponseEntity<List<ProductSalesDto>> topProdotti(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<ProductSalesDto> list = statsService.prodottiPiùVenduti(period, from, to, limit);
        return ResponseEntity.ok(list);
    }

    // Prodotti meno venduti
    @GetMapping("/prodotti/bottom")
    public ResponseEntity<List<ProductSalesDto>> bottomProdotti(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<ProductSalesDto> list = statsService.prodottiMenoVenduti(period, from, to, limit);
        return ResponseEntity.ok(list);
    }

    // Delta per singola sessione
    @GetMapping("/sessione/{id}/delta")
    public ResponseEntity<SessionDeltaDto> deltaPerSessione(@PathVariable("id") Long id) {
        SessionDeltaDto dto = statsService.deltaSessione(id);
        return ResponseEntity.ok(dto);
    }
}
