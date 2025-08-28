package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.Sessione;

public interface SessioneService {
    List<Sessione> findAll();
    Sessione findById(Long id);
    Sessione save(Sessione sessione);
    Sessione update(Long id, Sessione sessione);
    void delete(Long id);
}
