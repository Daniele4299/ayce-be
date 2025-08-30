package com.db.ayce.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Tavolo;
import com.db.ayce.be.repository.OrdineRepository;
import com.db.ayce.be.repository.TavoloRepository;
import com.db.ayce.be.service.TavoloService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TavoloServiceImpl implements TavoloService {

    private final TavoloRepository tavoloRepository;
    private final OrdineRepository ordineRepository;

    @Override
    public List<Tavolo> findAll() {
        return tavoloRepository.findAll();
    }

    @Override
    public Tavolo findById(Integer id) {
        return tavoloRepository.findById(id).orElse(null);
    }

    @Override
    public Tavolo save(Tavolo tavolo) {
        return tavoloRepository.save(tavolo);
    }

    @Override
    public Tavolo update(Integer id, Tavolo tavolo) {
        tavolo.setId(id);
        return tavoloRepository.save(tavolo);
    }

    @Override
    public void delete(Integer id) {
        tavoloRepository.deleteById(id);
    }

	@Override
	public Tavolo findByNumero(Integer numero) {
		return tavoloRepository.findByNumero(numero);
	}

	@Override
	public List<Ordine> findBySessione(Long sessioneId) {
	    return ordineRepository.findBySessioneId(sessioneId);
	}

}
