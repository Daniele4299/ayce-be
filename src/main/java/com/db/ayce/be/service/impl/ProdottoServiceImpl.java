package com.db.ayce.be.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.db.ayce.be.dto.UltimoOrdineDto;
import com.db.ayce.be.entity.Prodotto;
import com.db.ayce.be.repository.ProdottoRepository;
import com.db.ayce.be.service.ProdottoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdottoServiceImpl implements ProdottoService {

    private final ProdottoRepository prodottoRepository;

    @Override
    public List<Prodotto> findAll() {
        return prodottoRepository.findAll();
    }

    @Override
    public Prodotto findById(Long id) {
        return prodottoRepository.findById(id).orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        return prodottoRepository.save(prodotto);
    }

    @Override
    public Prodotto update(Long id, Prodotto prodotto) {
        prodotto.setId(id);
        return prodottoRepository.save(prodotto);
    }

    @Override
    public void delete(Long id) {
        prodottoRepository.deleteById(id);
    }

	@Override
	public List<UltimoOrdineDto> getProdottiUtilizzatiUltimoServizio(LocalDateTime inizio, LocalDateTime fine) {
	    return prodottoRepository.getQuantitaOrdinataGiornata(inizio, fine);
	}

}
