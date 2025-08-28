package com.db.ayce.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.ayce.be.dto.OrdineDto;
import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.OrdineRepository;
import com.db.ayce.be.repository.SessioneRepository;
import com.db.ayce.be.repository.UtenteProdottoRepository;
import com.db.ayce.be.service.ComandeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComandeServiceImpl implements ComandeService {

	@Autowired
	UtenteProdottoRepository utenteProdottoRepository;
	
	@Autowired
	SessioneRepository sessioneRepository;
	
	@Autowired
	OrdineRepository ordineRepository;
	

	@Override
	public List<OrdineDto> getComandeFiltrate(Long id, boolean soloAssegnati, boolean nascondiConsegnati) {
	    List<Long> productsList = soloAssegnati
	            ? utenteProdottoRepository.findProdottoIdsByUtenteIdAndRiceveComandaTrue(id)
	            : utenteProdottoRepository.findProdottoIdsByUtenteId(id);

	    List<Sessione> sessioneList = sessioneRepository.findByStatoIgnoreCase("ATTIVA");

	    List<Ordine> ordiniRaw = ordineRepository.findBySessioneIn(sessioneList);

	    List<Ordine> ordiniFiltrati = ordiniRaw.stream()
	        .filter(o -> (!soloAssegnati || productsList.contains(o.getProdotto().getId())))
	        .filter(o -> !nascondiConsegnati || !o.getFlagConsegnato()) 
	        .toList();

	    return ordiniFiltrati.stream()
	        .map(o -> new OrdineDto(
	                o.getId(),
	                o.getSessione().getTavolo(),
	                o.getProdotto(),
	                o.getQuantita(),
	                o.getOrario(),
	                o.getFlagConsegnato(),
	                o.getSessione() != null ? o.getSessione().getNumeroPartecipanti() : null
	        ))
	        .toList();
	}



	@Override
	public OrdineDto updateOrdineConsegnato(Long id, OrdineDto ordineDto, Utente utente) {
		Optional<Ordine> optionalOrdine = ordineRepository.findById(id);

	    if (optionalOrdine.isEmpty()) {
	        throw new RuntimeException("Ordine non trovato con id " + id);
	    }

	    Ordine ordine = optionalOrdine.get();

	    if (ordineDto.getFlagConsegnato() != null) {
	        ordine.setFlagConsegnato(ordineDto.getFlagConsegnato());
	    }

	    ordine = ordineRepository.save(ordine);

	    OrdineDto dto = new OrdineDto();
	    dto.setId(ordine.getId());
	    dto.setTavolo(ordine.getTavolo());
	    dto.setProdotto(ordine.getProdotto());
	    dto.setQuantita(ordine.getQuantita());
	    dto.setOrario(ordine.getOrario());
	    dto.setFlagConsegnato(ordine.getFlagConsegnato());
	    dto.setNumeroPartecipanti(
	        ordine.getSessione() != null ? ordine.getSessione().getNumeroPartecipanti() : null
	    );

	    return dto;
	}
}
