package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.Ordine;

public interface OrdineService {
    List<Ordine> findAll();
    Ordine findById(Integer id);
    Ordine save(Ordine ordine);
    Ordine update(Integer id, Ordine ordine);
    void delete(Integer id);
}
