package com.db.ayce.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.service.SessioneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sessioni")
@RequiredArgsConstructor
public class SessioneController {

    private final SessioneService sessioneService;

    @GetMapping
    public List<Sessione> getAllSessioni() {
        return sessioneService.findAll();
    }

    @GetMapping("/{id}")
    public Sessione getSessioneById(@PathVariable Long id) {
        return sessioneService.findById(id);
    }

    @PostMapping
    public Sessione createSessione(@RequestBody Sessione sessione) {
        return sessioneService.save(sessione);
    }

    @PutMapping("/{id}")
    public Sessione updateSessione(@PathVariable Long id, @RequestBody Sessione sessione) {
        return sessioneService.update(id, sessione);
    }

    @DeleteMapping("/{id}")
    public void deleteSessione(@PathVariable Long id) {
        sessioneService.delete(id);
    }
    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getSessioneResocontoById(@PathVariable Long id) {
        byte[] pdfBytes = sessioneService.generatePdfResoconto(id);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=sessione_" + id + ".pdf")
                .body(pdfBytes);
    }

    
}
