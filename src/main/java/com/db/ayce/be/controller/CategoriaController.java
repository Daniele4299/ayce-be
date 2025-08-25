package com.db.ayce.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.ayce.be.entity.Categoria;
import com.db.ayce.be.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorie")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // GET: tutte le categorie
    @GetMapping
    public List<Categoria> getAllCategorie() {
        return categoriaService.findAll();
    }

    // GET: categoria per id
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            return ResponseEntity.ok(categoria);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST: crea nuova categoria
    @PostMapping
    public Categoria createCategoria(@RequestBody Categoria categoria) {
        return categoriaService.save(categoria);
    }

    // PUT: aggiorna categoria esistente
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> updateCategoria(@PathVariable Long id, @RequestBody Categoria updatedCategoria) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoria.setNome(updatedCategoria.getNome());
            return ResponseEntity.ok(categoriaService.save(categoria));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: elimina categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
