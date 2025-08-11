package com.db.ayce.be.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = "multipart/form-data")
    public Prodotto createProdotto(
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam("tipo") String tipo,
            @RequestParam("immagine") MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = new Prodotto();
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);
        p.setTipo(tipo);
        p.setImmagine(immagineFile.getBytes());
        return prodottoService.save(p);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Prodotto updateProdotto(
            @PathVariable Integer id,
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "immagine", required = false) MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = prodottoService.findById(id);
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);
        p.setTipo(tipo);
        if (immagineFile != null && !immagineFile.isEmpty()) {
            p.setImmagine(immagineFile.getBytes());
        }
        return prodottoService.save(p);
    }

    @GetMapping("/{id}/immagine")
    public ResponseEntity<byte[]> getProdottoImage(@PathVariable Integer id) {
        Prodotto p = prodottoService.findById(id);
        if (p.getImmagine() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // o JPEG a seconda del formato
                .body(p.getImmagine());
    }

    @DeleteMapping("/{id}")
    public void deleteProdotto(@PathVariable Integer id) {
        prodottoService.delete(id);
    }
}
