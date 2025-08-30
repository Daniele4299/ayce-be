package com.db.ayce.be.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TavoloTempService {

    // TavoloId -> (ProdottoId -> Quantità)
    private final Map<Integer, Map<Long, Integer>> ordineTemp = new ConcurrentHashMap<>();

    public void addItem(Integer tavoloId, Long prodottoId, int quantita) {
        ordineTemp.computeIfAbsent(tavoloId, k -> new ConcurrentHashMap<>())
                  .merge(prodottoId, quantita, Integer::sum);
    }

    public void removeItem(Integer tavoloId, Long prodottoId, int quantita) {
        Map<Long, Integer> tavoloOrdine = ordineTemp.get(tavoloId);
        if (tavoloOrdine != null) {
            tavoloOrdine.merge(prodottoId, -quantita, Integer::sum);
            tavoloOrdine.entrySet().removeIf(e -> e.getValue() <= 0);
        }
    }

    public Map<Long, Integer> getOrdineTemp(Integer tavoloId) {
        return ordineTemp.getOrDefault(tavoloId, Map.of());
    }

    public void clearOrdine(Integer tavoloId) {
        ordineTemp.remove(tavoloId);
    }

    public int getTotalePortate(Integer tavoloId) {
        return ordineTemp.getOrDefault(tavoloId, Map.of())
                         .values()
                         .stream()
                         .mapToInt(Integer::intValue)
                         .sum();
    }
}
