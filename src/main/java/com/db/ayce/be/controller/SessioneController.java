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
    public Sessione getSessioneById(@PathVariable Integer id) {
        return sessioneService.findById(id);
    }

    @PostMapping
    public Sessione createSessione(@RequestBody Sessione sessione) {
        return sessioneService.save(sessione);
    }

    @PutMapping("/{id}")
    public Sessione updateSessione(@PathVariable Integer id, @RequestBody Sessione sessione) {
        return sessioneService.update(id, sessione);
    }

    @DeleteMapping("/{id}")
    public void deleteSessione(@PathVariable Integer id) {
        sessioneService.delete(id);
    }
}
