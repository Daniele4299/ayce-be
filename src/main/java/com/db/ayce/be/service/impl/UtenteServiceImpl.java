package com.db.ayce.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;
import com.db.ayce.be.service.UtenteService;

@Service
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository repository;

    public UtenteServiceImpl(UtenteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Utente> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Utente> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<Utente> findAll() {
        return repository.findAll();
    }

    @Override
    public Utente save(Utente utente) {
        return repository.save(utente);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
