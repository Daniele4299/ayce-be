package com.db.ayce.be.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.service.TavoloTempService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TavoloTempController {

    private final TavoloTempService tavoloTempService;

    @GetMapping("/api/tavoli/{tavoloId}/ordine-temporaneo")
    public Map<Long, Integer> getOrdineTemporaneo(@PathVariable Integer tavoloId) {
        return tavoloTempService.getOrdineTemp(tavoloId);
    }
}
