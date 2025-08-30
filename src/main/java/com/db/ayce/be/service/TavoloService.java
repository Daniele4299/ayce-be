package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Tavolo;

public interface TavoloService {
    List<Tavolo> findAll();
    Tavolo findById(Integer id);
    Tavolo save(Tavolo tavolo);
    Tavolo update(Integer id, Tavolo tavolo);
    void delete(Integer id);
    Tavolo findByNumero(Integer numero);
    List<Ordine> findBySessione(Long sessioneId);
}
