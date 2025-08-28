package com.db.ayce.be.service;

import java.util.List;

import com.db.ayce.be.dto.OrdineDto;
import com.db.ayce.be.entity.Utente;

public interface ComandeService {
	List<OrdineDto> getComandeFiltrate(Long id, boolean soloAssegnati, boolean nascondiConsegnati);
	OrdineDto updateOrdineConsegnato(Long id, OrdineDto ordineDto, Utente utente);
}
