package com.db.ayce.be.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.service.OrdineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ordini")
@RequiredArgsConstructor
public class OrdineController {

    private final OrdineService ordineService;

    @GetMapping
    public List<Ordine> getAllOrdini() {
        return ordineService.findAll();
    }

    @GetMapping("/{id}")
    public Ordine getOrdineById(@PathVariable Integer id) {
        return ordineService.findById(id);
    }

    @PostMapping
    public Ordine createOrdine(@RequestBody Ordine ordine) {
        return ordineService.save(ordine);
    }

    @PutMapping("/{id}")
    public Ordine updateOrdine(@PathVariable Integer id, @RequestBody Ordine ordine) {
        return ordineService.update(id, ordine);
    }

    @DeleteMapping("/{id}")
    public void deleteOrdine(@PathVariable Integer id) {
        ordineService.delete(id);
    }
}
