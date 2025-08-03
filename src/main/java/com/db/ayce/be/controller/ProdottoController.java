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

import com.db.ayce.be.entity.Prodotto;
import com.db.ayce.be.service.ProdottoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prodotti")
@RequiredArgsConstructor
public class ProdottoController {

    private final ProdottoService prodottoService;

    @GetMapping
    public List<Prodotto> getAllProdotti() {
        return prodottoService.findAll();
    }

    @GetMapping("/{id}")
    public Prodotto getProdottoById(@PathVariable Integer id) {
        return prodottoService.findById(id);
    }

    @PostMapping
    public Prodotto createProdotto(@RequestBody Prodotto prodotto) {
        return prodottoService.save(prodotto);
    }

    @PutMapping("/{id}")
    public Prodotto updateProdotto(@PathVariable Integer id, @RequestBody Prodotto prodotto) {
        return prodottoService.update(id, prodotto);
    }

    @DeleteMapping("/{id}")
    public void deleteProdotto(@PathVariable Integer id) {
        prodottoService.delete(id);
    }
}
