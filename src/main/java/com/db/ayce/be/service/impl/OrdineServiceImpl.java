package com.db.ayce.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.repository.OrdineRepository;
import com.db.ayce.be.service.OrdineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {

    private final OrdineRepository ordineRepository;

    @Override
    public List<Ordine> findAll() {
        return ordineRepository.findAll();
    }

    @Override
    public Ordine findById(Integer id) {
        return ordineRepository.findById(id).orElse(null);
    }

    @Override
    public Ordine save(Ordine ordine) {
        return ordineRepository.save(ordine);
    }

    @Override
    public Ordine update(Integer id, Ordine ordine) {
        ordine.setId(id);
        return ordineRepository.save(ordine);
    }

    @Override
    public void delete(Integer id) {
        ordineRepository.deleteById(id);
    }
}
