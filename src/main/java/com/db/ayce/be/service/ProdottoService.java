package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.Prodotto;

public interface ProdottoService {
    List<Prodotto> findAll();
    Prodotto findById(Integer id);
    Prodotto save(Prodotto prodotto);
    Prodotto update(Integer id, Prodotto prodotto);
    void delete(Integer id);
}
