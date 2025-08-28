package com.db.ayce.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.dto.OrdineDto;
import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;
import com.db.ayce.be.service.ComandeService;
import com.db.ayce.be.utils.AuthUtils;
import com.db.ayce.be.utils.Constants;

@RestController
@RequestMapping("/api/private/comande")
public class ComandeController {

	@Autowired
    ComandeService comandeService;
	@Autowired
    UtenteRepository utenteRepository;
	@Autowired
	AuthUtils authUtils;

    /**
     * Recupera gli ordini filtrati per l'utente corrente.
     * @param soloAssegnati Se true, ritorna solo prodotti assegnati all'utente
     * @return lista di ordini DTO già arricchiti con tavolo e numero partecipanti
     */
	@GetMapping("/filtrate")
	public List<OrdineDto> getOrdiniFiltrati(
	        @RequestParam(defaultValue = "true") boolean soloAssegnati,
	        @RequestParam(defaultValue = "false") boolean nascondiConsegnati) {

	    Utente utente = authUtils.getCurrentUserOrThrow(Constants.ROLE_ADMIN, Constants.ROLE_DIPEN);
	    return comandeService.getComandeFiltrate(utente.getId(), soloAssegnati, nascondiConsegnati);
	}

    
    /**
     * Aggiorna solo il flagConsegnato di un ordine tramite OrdineDto
     */
    @PutMapping("/consegna/{id}")
    public OrdineDto updateOrdineConsegnato(@PathVariable Long id, @RequestBody OrdineDto ordineDto) {
    	Utente utente = authUtils.getCurrentUserOrThrow(Constants.ROLE_ADMIN, Constants.ROLE_DIPEN);
    	return comandeService.updateOrdineConsegnato(id, ordineDto, utente);

    }
}
