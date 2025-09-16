package com.db.ayce.be.service;

import java.time.LocalDateTime;
import java.util.List;

import com.db.ayce.be.dto.UltimoOrdineDto;
import com.db.ayce.be.entity.Prodotto;

public interface ProdottoService {
    List<Prodotto> findAll();
    Prodotto findById(Long id);
    Prodotto save(Prodotto prodotto);
    Prodotto update(Long id, Prodotto prodotto);
    void delete(Long id);
    List<UltimoOrdineDto> getProdottiUtilizzatiUltimoServizio(LocalDateTime inizio, LocalDateTime fine);
	List<Prodotto> findDeleted();
	Prodotto restore(Long id);
    	
}
