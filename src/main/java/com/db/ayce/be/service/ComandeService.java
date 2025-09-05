package com.db.ayce.be.service;

import java.util.List;
import java.util.Map;

import com.db.ayce.be.dto.OrdineDto;

public interface ComandeService {
	List<OrdineDto> getComandeFiltrate(Long id, boolean soloAssegnati, boolean nascondiConsegnati);
	void toggleConsegnato(Long id, Map<String, Object> body);
}
