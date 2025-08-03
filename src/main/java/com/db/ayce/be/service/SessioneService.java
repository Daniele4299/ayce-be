package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.Sessione;

public interface SessioneService {
    List<Sessione> findAll();
    Sessione findById(Integer id);
    Sessione save(Sessione sessione);
    Sessione update(Integer id, Sessione sessione);
    void delete(Integer id);
}
