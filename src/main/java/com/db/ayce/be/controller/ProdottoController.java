package com.db.ayce.be.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.db.ayce.be.entity.Categoria;
import com.db.ayce.be.entity.Prodotto;
import com.db.ayce.be.service.CategoriaService;
import com.db.ayce.be.service.ImageService;
import com.db.ayce.be.service.ProdottoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prodotti")
@RequiredArgsConstructor
public class ProdottoController {

    private final ProdottoService prodottoService;
    private final CategoriaService categoriaService;
    private final ImageService imageService;

    @GetMapping
    public List<Prodotto> getAllProdotti() {
        return prodottoService.findAll();
    }

    @GetMapping("/{id}")
    public Prodotto getProdottoById(@PathVariable Long id) {
        return prodottoService.findById(id);
    }

    @PostMapping(consumes = "multipart/form-data")
    public Prodotto createProdotto(
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "isPranzo", defaultValue = "true") Boolean isPranzo,
            @RequestParam(value = "isCena", defaultValue = "true") Boolean isCena,
            @RequestParam(value = "isAyce", defaultValue = "true") Boolean isAyce,
            @RequestParam(value = "isCarta", defaultValue = "true") Boolean isCarta,
            @RequestParam(value = "isLimitedPartecipanti", defaultValue = "false") Boolean isLimitedPartecipanti,
            @RequestParam(value = "immagine", required = false) MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = new Prodotto();
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);

        if (categoriaId != null) {
            Categoria categoria = categoriaService.findById(categoriaId);
            p.setCategoria(categoria);
        }

        p.setIsPranzo(isPranzo);
        p.setIsCena(isCena);
        p.setIsAyce(isAyce);
        p.setIsCarta(isCarta);
        p.setIsLimitedPartecipanti(isLimitedPartecipanti);

        // salvo prodotto senza immagine per ottenere ID
        Prodotto saved = prodottoService.save(p);

        if (immagineFile != null && !immagineFile.isEmpty()) {
            String relativePath = imageService.saveProductImage(immagineFile, saved.getId());
            saved.setImmagine(relativePath);
            saved = prodottoService.save(saved);
        }

        return saved;
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Prodotto updateProdotto(
            @PathVariable Long id,
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "isPranzo", defaultValue = "true") Boolean isPranzo,
            @RequestParam(value = "isCena", defaultValue = "true") Boolean isCena,
            @RequestParam(value = "isAyce", defaultValue = "true") Boolean isAyce,
            @RequestParam(value = "isCarta", defaultValue = "true") Boolean isCarta,
            @RequestParam(value = "isLimitedPartecipanti", defaultValue = "false") Boolean isLimitedPartecipanti,
            @RequestParam(value = "immagine", required = false) MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = prodottoService.findById(id);
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);

        if (categoriaId != null) {
            Categoria categoria = categoriaService.findById(categoriaId);
            p.setCategoria(categoria);
        } else {
            p.setCategoria(null);
        }

        p.setIsPranzo(isPranzo);
        p.setIsCena(isCena);
        p.setIsAyce(isAyce);
        p.setIsCarta(isCarta);
        p.setIsLimitedPartecipanti(isLimitedPartecipanti);

        if (immagineFile != null && !immagineFile.isEmpty()) {
            String relativePath = imageService.saveProductImage(immagineFile, p.getId());
            p.setImmagine(relativePath);
        }

        return prodottoService.save(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProdotto(@PathVariable Long id) {
        prodottoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
