package com.db.ayce.be.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TavoloTempService {

    // Stato temporaneo per ogni tavolo: tavoloId -> (prodottoId -> qty)
    private final Map<Integer, Map<Long, Integer>> ordineTempMap = new HashMap<>();

    public synchronized Map<Long, Integer> getOrdineTemp(Integer tavoloId) {
        return ordineTempMap.getOrDefault(tavoloId, new HashMap<>());
    }

    public synchronized void addItem(Integer tavoloId, Long prodottoId, int qty) {
        Map<Long, Integer> ordine = ordineTempMap.computeIfAbsent(tavoloId, k -> new HashMap<>());
        ordine.merge(prodottoId, qty, Integer::sum);
    }

    public synchronized void removeItem(Integer tavoloId, Long prodottoId, int qty) {
        Map<Long, Integer> ordine = ordineTempMap.computeIfAbsent(tavoloId, k -> new HashMap<>());
        ordine.compute(prodottoId, (k, v) -> Math.max((v == null ? 0 : v) - qty, 0));
    }

    public synchronized void clearOrdine(Integer tavoloId) {
        ordineTempMap.remove(tavoloId);
    }
}
