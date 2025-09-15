package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.entity.CostoProdotto;

public interface CostoProdottoService {
    List<CostoProdotto> getAll();
    CostoProdotto saveOrUpdate(Long prodottoId, Double costo);
}

