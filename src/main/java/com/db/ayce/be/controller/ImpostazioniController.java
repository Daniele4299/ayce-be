package com.db.ayce.be.controller;

import com.db.ayce.be.entity.Impostazioni;
import com.db.ayce.be.service.ImpostazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/impostazioni")
public class ImpostazioniController {

    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniService service) {
        this.service = service;
    }

    @GetMapping
    public List<Impostazioni> getAll() {
        return service.findAll();
    }

    @GetMapping("/{chiave}")
    public ResponseEntity<Impostazioni> getByChiave(@PathVariable String chiave) {
        return service.findByChiave(chiave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Impostazioni create(@RequestBody Impostazioni Impostazioni) {
        return service.save(Impostazioni);
    }

    @PutMapping("/{chiave}")
    public ResponseEntity<Impostazioni> update(@PathVariable String chiave, @RequestBody String valore) {
        Impostazioni updated = service.update(chiave, valore);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}
